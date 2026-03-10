package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AfterSalesTypeEnum {

    REFUNDED(1, "退货"),

    EXCHANGE(2, "换货"),

    REPAIR(3, "维修"),

    REISSUE(4, "补发"),

    ;

    private Integer code;

    private String description;

    public static String getDescription(Integer code) {
        return Arrays.asList(AfterSalesTypeEnum.values())
                .stream().filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .map(AfterSalesTypeEnum::getDescription)
                .orElse("未知");
    }

}
