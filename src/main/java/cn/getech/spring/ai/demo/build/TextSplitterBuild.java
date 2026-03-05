package cn.getech.spring.ai.demo.build;

import cn.getech.spring.ai.demo.check.TextSplitterCheck;
import cn.getech.spring.ai.demo.enums.SplitterTypeEnum;
import cn.getech.spring.ai.demo.utils.TextSplitterUtils;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * @author 11030
 */
@Component
public class TextSplitterBuild {

    @Autowired
    private TokenTextSplitter tokenTextSplitter;

    @Autowired
    private RecursiveCharacterTextSplitter recursiveTextSplitter;

    @Autowired
    private SentenceSplitter sentenceSplitter;

    @Resource(name = "paragraphTextSplitter")
    private TextSplitter paragraphTextSplitter;

    @Resource(name = "characterTextSplitter")
    private TextSplitter characterTextSplitter;

    @Resource(name = "markdownTextSplitter")
    private TextSplitter markdownTextSplitter;

    @Resource(name = "htmlTextSplitter")
    private TextSplitter htmlTextSplitter;

    @Resource(name = "jsonTextSplitter")
    private TextSplitter jsonTextSplitter;

    @Resource(name = "chineseTextSplitter")
    private TextSplitter chineseTextSplitter;

    @Resource(name = "codeTextSplitter")
    private TextSplitter codeTextSplitter;

    /**
     * 中文优化预处理
     */
    public String preprocessChineseText(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }

        // 处理中文标点，确保正确分句
        text = text.replaceAll("([。！？；：])", "$1\n");

        // 处理中文空格，确保词语不被切断
        text = text.replaceAll("([\\u4e00-\\u9fa5])([a-zA-Z0-9])", "$1 $2");
        text = text.replaceAll("([a-zA-Z0-9])([\\u4e00-\\u9fa5])", "$1 $2");

        // 处理中文数字和单位之间的空格
        text = text.replaceAll("([0-9])([\\u4e00-\\u9fa5])", "$1 $2");
        text = text.replaceAll("([\\u4e00-\\u9fa5])([0-9])", "$1 $2");

        return text;
    }

    /**
     * 多粒度分层分割（根据不同类型使用不同的分割策略）
     */
    public List<String> processMultiGranularitySplit(String text, String algorithm) {
        List<String> chunks = new ArrayList<>();
        TextSplitterCheck.validateAlgorithm(algorithm);

        // 根据不同类型使用不同的分割策略
        SplitterTypeEnum splitterType = SplitterTypeEnum.valueOf(algorithm);
        switch (splitterType) {
            case MARKDOWN:
                // Markdown优先使用Markdown分割器
                Document markdownDoc = createDocument(text, null);
                List<Document> markdownDocs = splitDocument(markdownTextSplitter, markdownDoc);
                for (Document doc : markdownDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitMarkdownContent(text);
                }
                break;
            case HTML:
                // HTML优先使用HTML分割器
                Document htmlDoc = createDocument(text, null);
                List<Document> htmlDocs = splitDocument(htmlTextSplitter, htmlDoc);
                for (Document doc : htmlDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitHtmlContent(text);
                }
                break;
            case JSON:
                // JSON优先使用JSON分割器
                Document jsonDoc = createDocument(text, null);
                List<Document> jsonDocs = splitDocument(jsonTextSplitter, jsonDoc);
                for (Document doc : jsonDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitJsonContent(text);
                }
                break;
            case CODE:
                // 代码优先使用代码分割器
                Document codeDoc = createDocument(text, null);
                List<Document> codeDocs = splitDocument(codeTextSplitter, codeDoc);
                for (Document doc : codeDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitCodeContent(text);
                }
                break;
            case CHINESE:
                // 中文文本优先使用中文分割器
                Document chineseDoc = createDocument(text, null);
                List<Document> chineseDocs = splitDocument(chineseTextSplitter, chineseDoc);
                for (Document doc : chineseDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitChineseContent(text);
                }
                break;
            case TOKEN:
                // 英文文本优先使用Token分割器
                Document tokenDoc = createDocument(text, null);
                List<Document> tokenDocs = splitDocument(tokenTextSplitter, tokenDoc);
                for (Document doc : tokenDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitTokenContent(text);
                }
                break;
            case PARAGRAPH:
                // 段落文本优先使用段落分割器
                Document paragraphDoc = createDocument(text, null);
                List<Document> paragraphDocs = splitDocument(paragraphTextSplitter, paragraphDoc);
                for (Document doc : paragraphDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitByParagraph(text);
                }
                break;
            case SENTENCE:
                // 句子文本优先使用句子分割器
                Document sentenceDoc = createDocument(text, null);
                List<Document> sentenceDocs = splitDocument(sentenceSplitter, sentenceDoc);
                for (Document doc : sentenceDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitBySentence(text);
                }
                break;
            case RECURSIVE:
                // 递归字符分割（默认分割器）
                Document recursiveDoc = createDocument(text, null);
                List<Document> recursiveDocs = splitDocument(recursiveTextSplitter, recursiveDoc);
                for (Document doc : recursiveDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitGenericContent(text);
                }
                break;
            case CHARACTER:
            default:
                // 其他类型使用通用分割策略
                Document characterDoc = createDocument(text, null);
                List<Document> characterDocs = splitDocument(characterTextSplitter, characterDoc);
                for (Document doc : characterDocs) {
                    chunks.add(doc.getText());
                }
                if (chunks.isEmpty()) {
                    chunks = TextSplitterUtils.splitGenericContent(text);
                }
                break;
        }

        return chunks;
    }

    /**
     * 创建文档对象
     */
    public Document createDocument(String text, Map<String, Object> metadata) {
        return new Document(text, metadata != null ? metadata : new HashMap<>());
    }

    /**
     * 分割文档
     */
    public List<Document> splitDocument(TextSplitter splitter, Document document) {
        return splitter.apply(Collections.singletonList(document));
    }

}
