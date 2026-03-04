package cn.getech.spring.ai.demo.valid;

import cn.hutool.core.util.StrUtil;

/**
 * @author 11030
 */
public class ParagraphTextCheck {

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

}
