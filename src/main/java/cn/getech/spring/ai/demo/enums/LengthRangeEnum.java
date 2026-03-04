package cn.getech.spring.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum LengthRangeEnum {

    RANGE_0_100("0-100", 0, 99),
    RANGE_100_500("100-500", 100, 499),
    RANGE_500_1000("500-1000", 500, 999),
    RANGE_1000_2000("1000-2000", 1000, 1999),
    RANGE_2000_PLUS("2000+", 2000, Integer.MAX_VALUE);

    private String label;
    private int min;
    private int max;

    /**
     * 根据长度值查找对应的区间
     */
    public static LengthRangeEnum of(int length) {
        for (LengthRangeEnum range : values()) {
            if (length >= range.min && length <= range.max) {
                return range;
            }
        }
        return RANGE_2000_PLUS;
    }

}
