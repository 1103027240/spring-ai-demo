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
public enum ChatMessageTypeEnum {

     USER(1, "用户消息"),

     AI(2, "AI回复"),

     SYSTEM(3, "系统消息"),

     CUSTOMER_SERVICE(4, "客服消息"),

    ;

    private Integer id;

    private String text;

    public static String getText(Integer id) {
        return Arrays.asList(ChatMessageTypeEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(ChatMessageTypeEnum::getText)
                .orElse("未知");
    }

}
