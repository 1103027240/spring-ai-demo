package cn.getech.spring.ai.demo.valid;

import cn.hutool.core.util.StrUtil;

/**
 * @author 11030
 */
public class SentenceTextCheck {

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

}
