package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CursorSortDirectionEnum {

    DESC("DESC", "<", "降序"),

    ASC("ASC", ">", "升序"),

    ;

    private String id;

    private String text;

    private String detailText;

}
