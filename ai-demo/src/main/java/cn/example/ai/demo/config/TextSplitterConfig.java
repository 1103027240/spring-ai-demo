package cn.example.ai.demo.config;

import cn.example.ai.demo.config.text.*;
import cn.example.ai.demo.config.text.*;
import cn.example.ai.demo.function.TextSplitterFunction;
import cn.example.ai.demo.utils.TextSplitterUtils;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文本分割器配置类
 * @author 11030
 */
@Slf4j
@Configuration
public class TextSplitterConfig {

    @Autowired
    private TokenTextConfig tokenTextConfig;

    @Autowired
    private RecursiveCharacterTextConfig recursiveCharacterTextConfig;

    @Autowired
    private SentenceTextConfig sentenceTextConfig;

    @Autowired
    private ParagraphTextConfig paragraphTextConfig;

    @Autowired
    private CharacterTextConfig characterTextConfig;

    @Autowired
    private MarkdownTextConfig markdownTextConfig;

    @Autowired
    private HtmlTextConfig htmlTextConfig;

    @Autowired
    private JsonTextConfig jsonTextConfig;

    @Autowired
    private ChineseTextConfig chineseTextConfig;

    @Autowired
    private CodeTextConfig codeTextConfig;

    /**
     * Token 分割器（按 token 计数，适合英文）
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(
                tokenTextConfig.getChunkSize(),
                tokenTextConfig.getMinChunkSize(),
                tokenTextConfig.getMinChunkLength(),
                tokenTextConfig.getMaxNumChunks(),
                tokenTextConfig.isKeepSeparator());
    }

    /**
     * 递归字符分割器（默认使用，支持多级分隔符）
     */
    @Bean
    public RecursiveCharacterTextSplitter recursiveCharacterTextSplitter() {
        return new RecursiveCharacterTextSplitter(recursiveCharacterTextConfig.getChunkSize(), recursiveCharacterTextConfig.getPattern());
    }

    /**
     * 句子分割器（按句子粒度分割）
     */
    @Bean
    public SentenceSplitter sentenceSplitter() {
        return new SentenceSplitter(sentenceTextConfig.getChunkSize());
    }

    /**
     * 段落分割器（按自然段落分割）
     */
    @Bean("paragraphTextSplitter")
    public TextSplitter paragraphTextSplitter() {
        Pattern paragraphTextPattern = Pattern.compile(paragraphTextConfig.getPattern());
        return createTextSplitter(text ->
                TextSplitterUtils.splitParagraph(text, paragraphTextPattern, paragraphTextConfig.getChunkSize(), paragraphTextConfig.getChunkOverlap()));
    }

    /**
     * 字符分割器（固定长度字符分割）
     */
    @Bean("characterTextSplitter")
    public TextSplitter characterTextSplitter() {
        return createTextSplitter(text ->
                TextSplitterUtils.splitByCharacterCount(text, characterTextConfig.getChunkSize(), characterTextConfig.getChunkOverlap(), characterTextConfig.isCharacterPreserveWords()));
    }

    /**
     * Markdown 分割器（按标题层级分割）
     */
    @Bean("markdownTextSplitter")
    public TextSplitter markdownTextSplitter() {
        Pattern markdownTextPattern = Pattern.compile(markdownTextConfig.getPattern(), Pattern.MULTILINE);
        return createTextSplitter(text ->
                TextSplitterUtils.splitMarkdown(text, markdownTextPattern, markdownTextConfig.getChunkSize(), markdownTextConfig.getChunkOverlap()));
    }

    /**
     * HTML 分割器（按 HTML 标签分割）
     */
    @Bean("htmlTextSplitter")
    public TextSplitter htmlTextSplitter() {
        Pattern htmlTextPattern = Pattern.compile(htmlTextConfig.getPattern());
        return createTextSplitter(text ->
                TextSplitterUtils.splitHtml(text, htmlTextPattern, htmlTextConfig.getChunkSize(), htmlTextConfig.getChunkOverlap()));
    }

    /**
     * JSON 分割器（按 JSON 结构分割）
     */
    @Bean("jsonTextSplitter")
    public TextSplitter jsonTextSplitter() {
        Pattern jsonTextPattern = Pattern.compile(jsonTextConfig.getPattern());
        return createTextSplitter(text ->
                TextSplitterUtils.splitJson(text, jsonTextPattern));
    }

    /**
     * 中文优化分割器（针对中文文本优化）
     */
    @Bean("chineseTextSplitter")
    public TextSplitter chineseTextSplitter() {
        Pattern chineseTextPattern = Pattern.compile(chineseTextConfig.getPattern());
        return createTextSplitter(text ->
                TextSplitterUtils.splitChinese(text, chineseTextPattern, chineseTextConfig.getChunkSize(), chineseTextConfig.getChunkOverlap()));
    }

    /**
     * 代码分割器（针对代码文件优化）
     */
    @Bean("codeTextSplitter")
    public TextSplitter codeTextSplitter() {
        Pattern codeTextPattern = Pattern.compile(codeTextConfig.getPattern());
        return createTextSplitter(text ->
                TextSplitterUtils.splitCode(text, codeTextPattern, codeTextConfig.getChunkSize(), codeTextConfig.getChunkOverlap()));
    }

    /**
     * 创建文本分割器（统一处理逻辑）
     */
    private TextSplitter createTextSplitter(TextSplitterFunction splitterFunction) {
        return new TextSplitter() {
            @Override
            protected List<String> splitText(String text) {
                if (StrUtil.isBlank(text)) {
                    return Collections.emptyList();
                }
                try {
                    return splitterFunction.apply(text);
                } catch (Exception e) {
                    throw new RuntimeException("创建文本分割器失败：" + e.getMessage(), e);
                }
            }
        };
    }

}
