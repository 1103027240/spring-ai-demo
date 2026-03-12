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
public enum ChatSessionStatusEnum {

    ENDED(0, "已结束"),

    ACTIVE(1, "进行中"),

    TRANSFERRED(2, "已转人工"),

    WAITING(3, "等待回复"),

    ;

    private Integer code;

    private String description;

    public static String getDescription(Integer code) {
        return Arrays.asList(ChatSessionStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .map(ChatSessionStatusEnum::getDescription)
                .orElse("未知");
    }

}
