package cn.getech.spring.ai.demo.valid;

import cn.hutool.core.util.StrUtil;
import java.util.Set;

/**
 * Markdown检测器：用于检测文本是否为Markdown格式
 * @author 11030
 */
public class MarkdownTextCheck {
    private static final Set<String> MARKDOWN_MARKERS = Set.of(
            "# ", "## ", "### ", "#### ", "##### ", "###### ",
            "```", "* ", "- ", "1. ", "> ",
            "[", "](", "!", "**", "__", "|", "---");

    /**
     * 检测文本是否为Markdown格式
     */
    public static boolean isMarkdown(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        int markerCount = 0;
        for (String marker : MARKDOWN_MARKERS) {
            if (text.contains(marker)) {
                markerCount++;
                // 如果找到多个Markdown标记，直接返回true
                if (markerCount >= 2) {
                    return true;
                }
            }
        }

        // 对于只有一个标记的情况，进行更严格的检查
        if (markerCount == 1) {
            // 检查代码块、链接或图片等更明显的Markdown特征
            return text.contains("```") ||
                    (text.contains("[") && text.contains("](") && text.contains(")")) ||
                    text.contains("![");
        }

        return false;
    }

}
