package cn.getech.spring.ai.demo.valid;

import cn.hutool.core.util.StrUtil;

/**
 * @author 11030
 */
public class ChineseTextCheck {

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

}
