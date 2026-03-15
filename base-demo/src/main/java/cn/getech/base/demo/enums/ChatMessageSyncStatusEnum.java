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
public enum ChatMessageSyncStatusEnum {

    PENDING(0, "未同步"),

    SYNCING(1, "同步中"),

    SYNCED(2, "已同步"),

    FAILED(3, "同步失败"),

    SKIPPED(4, "跳过同步"),

    ;

    private Integer id;

    private String text;

    public static String getText(Integer id) {
        return Arrays.asList(ChatMessageSyncStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(ChatMessageSyncStatusEnum::getText)
                .orElse("未知");
    }

}
