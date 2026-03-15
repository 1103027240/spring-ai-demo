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
public enum MessageTaskStatusEnum {

    PENDING(0, "待处理"),

    PROCESSING(1, "处理中"),

    COMPLETED(2, "已完成"),

    FAILED(3,"已失败"),

    CANCELLED(4, "已取消"),

    ;

    private Integer id;

    private String text;

    public static String getText(Integer id) {
        return Arrays.asList(MessageTaskStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(MessageTaskStatusEnum::getText)
                .orElse("未知");
    }

}
