package cn.getech.spring.ai.demo.valid;

import cn.hutool.core.util.StrUtil;
import java.util.Set;

/**
 * HTML检测器：用于检测文本是否为HTML格式
 * @author 11030
 */
public class HtmlCheck {
    private static final Set<String> COMMON_HTML_TAGS = Set.of(
            "<html", "<head", "<body", "<div", "<p>", "<span", "<h1", "<h2", "<h3",
            "<h4", "<h5", "<h6", "<ul", "<ol", "<li", "<table", "<tr", "<td",
            "<form", "<input", "<button", "<a ", "<img");

    /**
     * 检测文本是否为HTML格式
     */
    public static boolean isHtml(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        String lowerText = text.toLowerCase();

        // 检查HTML文档类型声明
        if (lowerText.contains("<!doctype")) {
            return true;
        }

        // 检查HTML根元素
        if (lowerText.contains("<html")) {
            return true;
        }

        // 检查常见的HTML结构标签
        if (lowerText.contains("<head") && lowerText.contains("<body")) {
            return true;
        }

        // 检查其他常见的HTML标签
        int htmlTagCount = 0;
        for (String tag : COMMON_HTML_TAGS) {
            if (lowerText.contains(tag)) {
                htmlTagCount++;
                // 如果找到多个HTML标签，直接返回true
                if (htmlTagCount >= 2) {
                    return true;
                }
            }
        }

        return false;
    }

}
