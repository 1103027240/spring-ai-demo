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
public enum AfterSalesStatusEnum {

    PENDING(0, "待处理"),

    PROCESSING(1, "处理中"),

    COMPLETED(2, "已完成"),

    CLOSED(3, "已关闭"),

    ;

    private Integer code;

    private String description;

    public static String getDescription(Integer code) {
        return Arrays.asList(AfterSalesStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .map(AfterSalesStatusEnum::getDescription)
                .orElse("未知");
    }

}
