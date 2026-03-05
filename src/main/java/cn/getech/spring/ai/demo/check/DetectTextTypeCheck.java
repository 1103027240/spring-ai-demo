package cn.getech.spring.ai.demo.check;

import cn.getech.spring.ai.demo.enums.SplitterTypeEnum;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import java.util.Set;

/**
 * @author 11030
 */
public class DetectTextTypeCheck {

    private static final Set<String> MARKDOWN_MARKERS = Set.of(
            "# ", "## ", "### ", "#### ", "##### ", "###### ",
            "```", "* ", "- ", "1. ", "> ",
            "[", "](", "!", "**", "__", "|", "---");

    private static final Set<String> COMMON_HTML_TAGS = Set.of(
            "<html", "<head", "<body", "<div", "<p>", "<span", "<h1", "<h2", "<h3",
            "<h4", "<h5", "<h6", "<ul", "<ol", "<li", "<table", "<tr", "<td",
            "<form", "<input", "<button", "<a ", "<img");

    private static final Set<String> JAVA_MARKERS = Set.of(
            "public class", "package ", "import ", "public static",
            "int ", "String ", "System.out.println");

    private static final Set<String> JS_MARKERS = Set.of(
            "function ", "const ", "let ", "var ", "console.log");

    private static final Set<String> PYTHON_MARKERS = Set.of(
            "def ", "import ", "from ", "print(", "if __name__ == \"__main__\"");

    private static final Set<String> CPP_MARKERS = Set.of(
            "#include ", "int main", "cout <<", "printf(");

    private static final Set<String> COMMENT_MARKERS = Set.of(
            "//", "/*", "*/", "# ", "'''", "\"\"\"");

    private static final Set<String> STRUCTURE_MARKERS = Set.of(
            "{", "}", "(", ")", "[", "]");

    /**
     * 统一的文本类型检测方法
     * @param text 待检测的文本
     * @return 文本类型
     */
    public static String detectTextType(String text) {
        if (StrUtil.isBlank(text)) {
            return SplitterTypeEnum.CHARACTER.getId();
        }

        // 1. 检测代码
        if (DetectTextTypeCheck.isCode(text)) {
            return SplitterTypeEnum.CODE.getId();
        }

        // 2. 检测中文文本
        if (DetectTextTypeCheck.isChineseText(text)) {
            return SplitterTypeEnum.CHINESE.getId();
        }

        // 3. 检测英文文本（适合token分割）
        if (DetectTextTypeCheck.isToken(text)) {
            return SplitterTypeEnum.TOKEN.getId();
        }

        // 4. 检测段落
        if (DetectTextTypeCheck.isParagraph(text)) {
            return SplitterTypeEnum.PARAGRAPH.getId();
        }

        // 5. 检测句子
        if (DetectTextTypeCheck.isSentence(text)) {
            return SplitterTypeEnum.SENTENCE.getId();
        }

        // 6. 检测Markdown
        if (DetectTextTypeCheck.isMarkdown(text)) {
            return SplitterTypeEnum.MARKDOWN.getId();
        }

        // 7. 检测HTML
        if (DetectTextTypeCheck.isHtml(text)) {
            return SplitterTypeEnum.HTML.getId();
        }

        // 8. 检测JSON
        if (JSONUtil.isTypeJSON(text)) {
            return SplitterTypeEnum.JSON.getId();
        }

        // 9. 区分 character 和 recursive 类型
        // 与 TextSplitterConfig 配置一致，使用 TextSplitterCheck 中封装的判断方法
        if (DetectTextTypeCheck.isCharacter(text)) {
            return SplitterTypeEnum.CHARACTER.getId();
        } else if (DetectTextTypeCheck.isRecursive(text)) {
            return SplitterTypeEnum.RECURSIVE.getId();
        }

        // 默认为递归字符分割（推荐的默认分割器）
        return SplitterTypeEnum.RECURSIVE.getId();
    }

    /**
     * 判断是否适合使用Token分割器
     * Token分割器基于token计数，适合处理需要精确控制token数量的文本
     */
    public static boolean isToken(String text) {
        return TokenTextTypeCheck.isToken(text);
    }

    /**
     * 判断是否为句子
     */
    public static boolean isSentence(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        // 句子通常以句号、问号或感叹号结尾
        String trimmed = text.trim();
        return trimmed.endsWith(".") || trimmed.endsWith("。") ||
                trimmed.endsWith("?") || trimmed.endsWith("？") ||
                trimmed.endsWith("!") || trimmed.endsWith("！");
    }

    /**
     * 判断是否为段落
     */
    public static boolean isParagraph(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        // 段落通常包含多个句子，以句号、问号或感叹号结尾
        return text.contains(".") || text.contains("。") ||
                text.contains("?") || text.contains("？") ||
                text.contains("!") || text.contains("！");
    }

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

    /**
     * 判断是否为中文文本
     */
    public static boolean isChineseText(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        long chineseCount = text.chars()
                .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN)
                .count();
        double chineseRatio = (double) chineseCount / text.length();
        return chineseRatio > 0.5; // 中文比例超过50%
    }

    /**
     * 检测文本是否为代码格式
     */
    public static boolean isCode(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        // 检查代码块标记
        if (text.contains("```")) {
            return true;
        }

        int codeMarkerCount = 0;

        // 检查Java特征
        for (String marker : JAVA_MARKERS) {
            if (text.contains(marker)) {
                codeMarkerCount++;
                break;
            }
        }

        // 检查JavaScript特征
        for (String marker : JS_MARKERS) {
            if (text.contains(marker)) {
                codeMarkerCount++;
                break;
            }
        }

        // 检查Python特征
        for (String marker : PYTHON_MARKERS) {
            if (text.contains(marker)) {
                codeMarkerCount++;
                break;
            }
        }

        // 检查C/C++特征
        for (String marker : CPP_MARKERS) {
            if (text.contains(marker)) {
                codeMarkerCount++;
                break;
            }
        }

        // 检查代码注释
        for (String marker : COMMENT_MARKERS) {
            if (text.contains(marker)) {
                codeMarkerCount++;
                break;
            }
        }

        // 检查代码结构特征
        boolean hasStructure = false;
        int structureCount = 0;
        for (String marker : STRUCTURE_MARKERS) {
            if (text.contains(marker)) {
                structureCount++;
            }
        }
        if (structureCount >= 2) {
            hasStructure = true;
        }

        if (hasStructure) {
            codeMarkerCount++;
        }

        // 如果检测到多个代码特征，返回true
        return codeMarkerCount >= 2;
    }

    /**
     * 判断是否适合使用字符分割器
     * 字符分割器按固定长度分割，适合结构简单、长度适中的文本
     */
    public static boolean isCharacter(String text) {
        if (StrUtil.isBlank(text)) {
            return true;
        }

        // 检查文本长度
        if (text.length() < 300) {
            // 检查分隔符类型数量
            int delimiterTypes = countDelimiterTypes(text);
            return delimiterTypes < 2;
        }

        return false;
    }

    /**
     * 判断是否适合使用递归字符分割器
     * 递归字符分割器支持多级分隔符，适合复杂结构的文本
     */
    public static boolean isRecursive(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        // 检查分隔符类型数量
        int delimiterTypes = countDelimiterTypes(text);
        if (delimiterTypes >= 2) {
            return true;
        }

        // 对于较长的文本，默认使用递归字符分割器
        return text.length() >= 300;
    }

    /**
     * 统计文本中包含的不同类型分隔符数量
     */
    private static int countDelimiterTypes(String text) {
        int delimiterTypes = 0;
        
        boolean hasSpaces = text.contains(" ");
        boolean hasCommas = text.contains(",") || text.contains("，");
        boolean hasPeriods = text.contains(".") || text.contains("。");
        boolean hasSemicolons = text.contains(";" ) || text.contains("；");
        boolean hasExclamations = text.contains("!") || text.contains("！");
        boolean hasQuestions = text.contains("?") || text.contains("？");
        
        if (hasSpaces) {
            delimiterTypes++;
        }
        if (hasCommas) {
            delimiterTypes++;
        }
        if (hasPeriods) {
            delimiterTypes++;
        }
        if (hasSemicolons) {
            delimiterTypes++;
        }
        if (hasExclamations) {
            delimiterTypes++;
        }
        if (hasQuestions) {
            delimiterTypes++;
        }
        
        return delimiterTypes;
    }

}
