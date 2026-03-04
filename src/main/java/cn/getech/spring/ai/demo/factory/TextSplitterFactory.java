package cn.getech.spring.ai.demo.factory;

import cn.getech.spring.ai.demo.enums.SplitterTypeEnum;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文本分割器工厂类
 * @author 11030
 */
@Slf4j
@Component
public class TextSplitterFactory {

    /** 文本分割器默认算法 **/
    @Value("${rag.text-splitting.default-strategy:recursive}")
    private String defaultStrategy;

    /** Token 分割器（按 token 计数，适合英文） */
    @Autowired
    private TokenTextSplitter tokenTextSplitter;

    /** 递归字符分割器（默认使用，支持多级分隔符） */
    @Autowired
    private RecursiveCharacterTextSplitter recursiveTextSplitter;

    /** 句子分割器（按句子粒度分割） */
    @Autowired
    private SentenceSplitter sentenceSplitter;

    /** 段落分割器（按自然段落分割） */
    @Resource(name = "paragraphTextSplitter")
    private TextSplitter paragraphTextSplitter;

    /** 字符分割器（固定长度字符分割） */
    @Resource(name = "characterTextSplitter")
    private TextSplitter characterTextSplitter;

    /** Markdown 分割器（按标题层级分割） */
    @Resource(name = "markdownTextSplitter")
    private TextSplitter markdownTextSplitter;

    /** HTML 分割器（按 HTML 标签分割） */
    @Resource(name = "htmlTextSplitter")
    private TextSplitter htmlTextSplitter;

    /** JSON 分割器（保持 JSON 结构完整） */
    @Resource(name = "jsonTextSplitter")
    private TextSplitter jsonTextSplitter;

    /** 中文分割器（针对中文优化） */
    @Resource(name = "chineseTextSplitter")
    private TextSplitter chineseTextSplitter;

    /** 代码分割器（针对代码文件优化） */
    @Resource(name = "codeTextSplitter")
    private TextSplitter codeTextSplitter;

    /** 分割器 */
    private final Map<String, TextSplitter> splitterMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        registerSplitters(
                Map.entry(SplitterTypeEnum.TOKEN, tokenTextSplitter),
                Map.entry(SplitterTypeEnum.RECURSIVE, recursiveTextSplitter),
                Map.entry(SplitterTypeEnum.SENTENCE, sentenceSplitter),
                Map.entry(SplitterTypeEnum.PARAGRAPH, paragraphTextSplitter),
                Map.entry(SplitterTypeEnum.CHARACTER, characterTextSplitter),
                Map.entry(SplitterTypeEnum.MARKDOWN, markdownTextSplitter),
                Map.entry(SplitterTypeEnum.HTML, htmlTextSplitter),
                Map.entry(SplitterTypeEnum.JSON, jsonTextSplitter),
                Map.entry(SplitterTypeEnum.CHINESE, chineseTextSplitter),
                Map.entry(SplitterTypeEnum.CODE, codeTextSplitter)
        );
    }

    private void registerSplitters(Map.Entry<SplitterTypeEnum, TextSplitter>... entries) {
        for (Map.Entry<SplitterTypeEnum, TextSplitter> entry : entries) {
            SplitterTypeEnum type = entry.getKey();
            TextSplitter splitter = entry.getValue();
            splitterMap.put(type.getId(), splitter);
        }
    }

    /**
     * 获取分割器，不存在则报错
     */
    public TextSplitter getTextSplitter(String name) {
        return Optional.ofNullable(splitterMap.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported split algorithm: " + name));
    }

    /**
     * 获取分割器，不存在则返回默认
     */
    public TextSplitter getTextSplitterOrDefault(String name) {
        return Optional.ofNullable(splitterMap.get(name))
                .orElseGet(() -> splitterMap.get(defaultStrategy));
    }

}
