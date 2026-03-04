package cn.getech.spring.ai.demo.valid;

import cn.hutool.core.util.StrUtil;
import java.util.Set;

/**
 * 代码检测器：用于检测文本是否为代码格式
 * @author 11030
 */
public class CodeCheck {
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

}
