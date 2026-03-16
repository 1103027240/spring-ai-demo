package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CursorSortByEnum {

    SCORE("score", "分数"),

    CREATE_TIME("createTime", "创建时间"),

    ;

    private String id;

    private String text;

}
