package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum KnowledgeDocumentStatusEnum {

    DISABLED(0, "禁用", "disabled"),

    ENABLED(1, "启用", "enabled"),

    PENDING(2, "待审核", "pending"),

    DELETED(3, "已删除", "deleted"),

    ;

    private Integer id;

    private String text;

    private String code;

}
