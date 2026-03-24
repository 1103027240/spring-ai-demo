package cn.example.base.demo.check;

import cn.example.base.demo.enums.SplitterTypeEnum;
import cn.hutool.core.util.StrUtil;

/**
 * @author 11030
 */
public class TextSplitterCheck {

    /**
     * 验证算法参数
     */
    public static void validateAlgorithm(String algorithm) {
        if (StrUtil.isBlank(algorithm)) {
            throw new IllegalArgumentException("Algorithm cannot be blank");
        }

        boolean exist = SplitterTypeEnum.checkAlgorithmExist(algorithm);
        if(!exist){
            throw new IllegalArgumentException("Algorithm not exist : " + algorithm);
        }
    }

}
