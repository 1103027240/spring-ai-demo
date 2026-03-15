package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SearchModeEnum {

    VECTOR("vector", "向量搜索"),

    KEYWORD("keyword", "关键词搜索"),

    HYBRID("hybrid", "混合搜索"),

    ;

    private String id;

    private String text;

}
