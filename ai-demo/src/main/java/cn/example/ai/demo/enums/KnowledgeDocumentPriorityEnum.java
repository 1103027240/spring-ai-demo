package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum KnowledgeDocumentPriorityEnum {

    LOWEST(0, "最低", "lowest"),

    LOW(25, "低", "low"),

    MEDIUM(50, "中", "medium"),

    HIGH(75, "高", "high"),

    HIGHEST(100, "最高", "highest"),

    ;

    private Integer id;

    private String text;

    private String code;

}
