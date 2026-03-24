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
public enum MessageTaskSyncTypeEnum {

    INCREMENTAL(1, "增量同步"),

    FULL(2, "全量同步"),

    FORCE(3, "强制同步"),

    ;

    private Integer id;

    private String text;

    public static String getText(Integer id) {
        return Arrays.asList(MessageTaskSyncTypeEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(MessageTaskSyncTypeEnum::getText)
                .orElse("未知");
    }

}
