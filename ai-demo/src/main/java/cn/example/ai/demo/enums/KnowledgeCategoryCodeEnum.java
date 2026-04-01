package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum KnowledgeCategoryCodeEnum {

    FAQ("FAQ", "常见问题", "系统常见问题分类"),

    KNOWLEDGE_BASE("KNOWLEDGE_BASE", "知识库", "系统知识库分类"),

    POLICY("POLICY", "政策法规", "政策法规分类"),

    USER_GUIDE("USER_GUIDE", "使用指南", "用户使用指南"),

    ;

    private String id;

    private String text;

    private String detailText;

}
