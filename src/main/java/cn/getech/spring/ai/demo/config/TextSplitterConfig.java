package cn.getech.spring.ai.demo.config;

import cn.getech.spring.ai.demo.function.TextSplitterFunction;
import cn.getech.spring.ai.demo.utils.TextSplitterUtils;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文本分割器配置类
 * @author 11030
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TextSplitterConfig {

    /** Token 分割 - 块大小（token 数） */
    @Value("${rag.text-splitting.token.chunk-size:1000}")
    private final int tokenChunkSize;

    /** Token 分割 - 最小块大小 */
    @Value("${rag.text-splitting.token.min-chunk-size:20}")
    private final int tokenMinChunkSize;

    /** Token 分割 - 最小块长度 */
    @Value("${rag.text-splitting.token.min-chunk-length:50}")
    private final int tokenMinChunkLength;

    /** Token 分割 - 最大块数 */
    @Value("${rag.text-splitting.token.max-num-chunks:1}")
    private final int tokenMaxNumChunks;

    /** Token 分割 - 是否保留分隔符 */
    @Value("${rag.text-splitting.token.keep-separator:true}")
    private final boolean tokenKeepSeparator;

    /** 递归字符分割 - 块大小（字符数） */
    @Value("${rag.text-splitting.recursive.chunk-size:1000}")
    private final int recursiveChunkSize;

    /** 递归字符分割 - 分隔符数组 */
    @Value("${rag.text-splitting.recursive.pattern}")
    private String[] recursivePattern;

    /** 句子分割 - 块大小（字符数） */
    @Value("${rag.text-splitting.sentence.chunk-size:1000}")
    private final int sentenceChunkSize;

    /** 段落分割 - 段落匹配正则 */
    @Value("${rag.text-splitting.paragraph.pattern}")
    private final String paragraphPattern;

    @Value("${rag.text-splitting.paragraph.chunk-size:1200}")
    private final int paragraphChunkSize;

    @Value("${rag.text-splitting.paragraph.chunk-overlap:250}")
    private final int paragraphChunkOverlap;

    /** 字符分割 - 块大小（字符数） */
    @Value("${rag.text-splitting.character.chunk-size:1000}")
    private final int characterChunkSize;

    /** 字符分割 - 块重叠量 */
    @Value("${rag.text-splitting.character.chunk-overlap:200}")
    private final int characterChunkOverlap;

    @Value("${rag.text-splitting.character.preserve-words:true}")
    private final boolean characterPreserveWords;

    /** Markdown 分割 - 标题匹配正则 */
    @Value("${rag.text-splitting.markdown.pattern}")
    private final String markdownPattern;

    /** Markdown 分割 - 块大小（字符数） */
    @Value("${rag.text-splitting.markdown.chunk-size:1000}")
    private final int markdownChunkSize;

    /** Markdown 分割 - 块重叠量 */
    @Value("${rag.text-splitting.markdown.chunk-overlap:200}")
    private final int markdownChunkOverlap;

    /** HTML 分割 - 标签匹配正则 */
    @Value("${rag.text-splitting.html.pattern}")
    private final String htmlPattern;

    /** HTML 分割 - 块大小（字符数） */
    @Value("${rag.text-splitting.html.chunk-size:1200}")
    private final int htmlChunkSize;

    /** HTML 分割 - 块重叠量 */
    @Value("${rag.text-splitting.html.chunk-overlap:250}")
    private final int htmlChunkOverlap;

    /** JSON 分割 - JSON 对象匹配正则 */
    @Value("${rag.text-splitting.json.pattern}")
    private final String jsonPattern;

    @Value("${rag.text-splitting.chinese.pattern}")
    private final String chinesePattern;

    /** 中文分割 - 块大小（中文字符数） */
    @Value("${rag.text-splitting.chinese.chunk-size:1000}")
    private final int chineseChunkSize;

    /** 中文分割 - 块重叠量 */
    @Value("${rag.text-splitting.chinese.chunk-overlap:200}")
    private final int chineseChunkOverlap;

    @Value("${rag.text-splitting.code.pattern}")
    private final String codePattern;

    /** 代码分割 - 块大小（代码字符数） */
    @Value("${rag.text-splitting.code.chunk-size:1500}")
    private final int codeChunkSize;

    /** 代码分割 - 块重叠量 */
    @Value("${rag.text-splitting.code.chunk-overlap:300}")
    private final int codeChunkOverlap;

    /**
     * Token 分割器（按 token 计数，适合英文）
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(
                tokenChunkSize,
                tokenMinChunkSize,
                tokenMinChunkLength,
                tokenMaxNumChunks,
                tokenKeepSeparator);
    }

    /**
     * 递归字符分割器（默认使用，支持多级分隔符）
     */
    @Bean
    @Primary
    public RecursiveCharacterTextSplitter recursiveCharacterTextSplitter() {
        return new RecursiveCharacterTextSplitter(recursiveChunkSize, recursivePattern);
    }

    /**
     * 句子分割器（按句子粒度分割）
     */
    @Bean
    public SentenceSplitter sentenceSplitter() {
        return new SentenceSplitter(sentenceChunkSize);
    }

    /**
     * 段落分割器（按自然段落分割）
     */
    @Bean("paragraphTextSplitter")
    public TextSplitter paragraphTextSplitter() {
        Pattern paragraphTextPattern = Pattern.compile(paragraphPattern, Pattern.MULTILINE);
        return createTextSplitter(text ->
                TextSplitterUtils.splitParagraph(text, paragraphTextPattern, paragraphChunkSize, paragraphChunkOverlap));
    }

    /**
     * 字符分割器（固定长度字符分割）
     */
    @Bean("characterTextSplitter")
    public TextSplitter characterTextSplitter() {
        return createTextSplitter(text ->
                TextSplitterUtils.splitByCharacterCount(text, characterChunkSize, characterChunkOverlap, characterPreserveWords));
    }

    /**
     * Markdown 分割器（按标题层级分割）
     */
    @Bean("markdownTextSplitter")
    public TextSplitter markdownTextSplitter() {
        Pattern markdownTextPattern = Pattern.compile(markdownPattern, Pattern.MULTILINE);
        return createTextSplitter(text ->
                TextSplitterUtils.splitMarkdown(text, markdownTextPattern, markdownChunkSize, markdownChunkOverlap));
    }

    /**
     * HTML 分割器（按 HTML 标签分割）
     */
    @Bean("htmlTextSplitter")
    public TextSplitter htmlTextSplitter() {
        Pattern htmlTextPattern = Pattern.compile(htmlPattern);
        return createTextSplitter(text ->
                TextSplitterUtils.splitHtml(text, htmlTextPattern, htmlChunkSize, htmlChunkOverlap));
    }

    /**
     * JSON 分割器（按 JSON 结构分割）
     */
    @Bean("jsonTextSplitter")
    public TextSplitter jsonTextSplitter() {
        Pattern jsonTextPattern = Pattern.compile(jsonPattern);
        return createTextSplitter(text ->
                TextSplitterUtils.splitJson(text, jsonTextPattern));
    }

    /**
     * 中文优化分割器（针对中文文本优化）
     */
    @Bean("chineseTextSplitter")
    public TextSplitter chineseTextSplitter() {
        Pattern chineseTextPattern = Pattern.compile(chinesePattern);
        return createTextSplitter(text ->
                TextSplitterUtils.splitChinese(text, chineseTextPattern, chineseChunkSize, chineseChunkOverlap));
    }

    /**
     * 代码分割器（针对代码文件优化）
     */
    @Bean("codeTextSplitter")
    public TextSplitter codeTextSplitter() {
        Pattern codeTextPattern = Pattern.compile(codePattern);
        return createTextSplitter(text ->
                TextSplitterUtils.splitCode(text, codeTextPattern, codeChunkSize, codeChunkOverlap));
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
