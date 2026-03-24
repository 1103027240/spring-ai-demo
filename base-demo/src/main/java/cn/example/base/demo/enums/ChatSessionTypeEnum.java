package cn.example.base.demo.enums;

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
public enum ChatSessionTypeEnum {

    CONSULTATION(1, "咨询"),

    AFTER_SALES(2, "售后"),

    COMPLAINT(3, "投诉"),

    SUGGESTION(4, "建议"),

    OTHER(5,"其他"),

    ;

    private Integer id;

    private String text;

    public static String getText(Integer id) {
        return Arrays.asList(ChatSessionTypeEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(ChatSessionTypeEnum::getText)
                .orElse("未知");
    }

}
