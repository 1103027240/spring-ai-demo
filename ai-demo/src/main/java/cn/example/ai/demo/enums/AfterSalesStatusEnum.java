package cn.example.ai.demo.enums;

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

    PENDING(0, "待处理", "您的申请已提交，正在等待客服处理"),

    PROCESSING(1, "处理中", "客服正在处理您的申请，请耐心等待"),

    COMPLETED(2, "已完成", "您的售后申请已完成处理"),

    CLOSED(3, "已关闭", "您的售后申请已关闭"),

    CANCELED(4, "已取消", "您的售后申请已取消"),

    ;

    private Integer id;

    private String text;

    private String detailText;

    public static String getText(Integer id) {
        return Arrays.asList(AfterSalesStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(AfterSalesStatusEnum::getText)
                .orElse("未知");
    }

    public static String getDetailText(Integer id) {
        return Arrays.asList(AfterSalesStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(AfterSalesStatusEnum::getDetailText)
                .orElse("处理中");
    }

}
