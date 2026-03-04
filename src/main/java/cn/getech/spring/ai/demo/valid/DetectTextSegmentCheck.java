package cn.getech.spring.ai.demo.valid;

import cn.getech.spring.ai.demo.dto.TextSegmentDto;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * 检测文本中的类型分段
 * @author 11030
 */
public class DetectTextSegmentCheck {

    /**
     * 检测文本中的类型分段
     */
    public static List<TextSegmentDto> detectTextSegments(String text) {
        List<TextSegmentDto> segments = new ArrayList<>();
        if (StrUtil.isBlank(text)) {
            return segments;
        }

        // 检测Markdown代码块
        int codeBlockStart = text.indexOf("```");
        if (codeBlockStart != -1) {
            int codeBlockEnd = text.indexOf("```", codeBlockStart + 3);
            if (codeBlockEnd != -1) {
                // 代码块前的部分
                if (codeBlockStart > 0) {
                    String prefix = text.substring(0, codeBlockStart);
                    segments.addAll(detectTextSegments(prefix));
                }
                // 代码块部分
                String codeBlock = text.substring(codeBlockStart, codeBlockEnd + 3);
                segments.add(new TextSegmentDto(codeBlock, "code"));
                // 代码块后的部分
                if (codeBlockEnd + 3 < text.length()) {
                    String suffix = text.substring(codeBlockEnd + 3);
                    segments.addAll(detectTextSegments(suffix));
                }
                return segments;
            }
        }

        // 检测HTML结构
        if (HtmlTextCheck.isHtml(text)) {
            segments.add(new TextSegmentDto(text, "html"));
            return segments;
        }

        // 检测JSON结构
        if (JSONUtil.isTypeJSON(text)) {
            segments.add(new TextSegmentDto(text, "json"));
            return segments;
        }

        // 检测中文文本
        if (ChineseTextCheck.isChineseText(text)) {
            segments.add(new TextSegmentDto(text, "chinese"));
            return segments;
        }

        // 检测英文文本（适合token分割）
        if (TokenTextCheck.isToken(text)) {
            segments.add(new TextSegmentDto(text, "token"));
            return segments;
        }

        // 检测Markdown
        if (MarkdownTextCheck.isMarkdown(text)) {
            segments.add(new TextSegmentDto(text, "markdown"));
            return segments;
        }

        // 检测代码
        if (CodeTextCheck.isCode(text)) {
            segments.add(new TextSegmentDto(text, "code"));
            return segments;
        }

        // 检测段落
        if (ParagraphTextCheck.isParagraph(text)) {
            segments.add(new TextSegmentDto(text, "paragraph"));
            return segments;
        }

        // 检测句子
        if (SentenceTextCheck.isSentence(text)) {
            segments.add(new TextSegmentDto(text, "sentence"));
            return segments;
        }

        // 默认为普通文本（字符分割）
        segments.add(new TextSegmentDto(text, "character"));
        return segments;
    }

}
