package cn.getech.spring.ai.demo.valid;

import cn.hutool.core.util.StrUtil;

/**
 * @author 11030
 */
public class TokenTextCheck {

    /**
     * 判断是否为英文文本（适合使用token分割器）
     */
    public static boolean isToken(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        // 统计英文字符和空格的比例
        long englishCount = text.chars()
                .filter(c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == ' ')
                .count();
        double englishRatio = (double) englishCount / text.length();
        return englishRatio > 0.7; // 英文比例超过70%
    }

}
