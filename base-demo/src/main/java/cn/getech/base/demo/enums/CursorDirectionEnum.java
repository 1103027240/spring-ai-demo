package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Arrays;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CursorDirectionEnum {

    FIRST("first", "首页"),

    NEXT("next", "下一页"),

    PREV("prev", "上一页"),

    ;

    private String id;

    private String text;

    public static boolean isValidCursorDirection(String id) {
        return Arrays.asList(FIRST.id, NEXT.id, PREV.id).contains(id);
    }

}
