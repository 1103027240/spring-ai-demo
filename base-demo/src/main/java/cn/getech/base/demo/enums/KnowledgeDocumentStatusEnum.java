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
public enum KnowledgeDocumentStatusEnum {

    DISABLED(0, "禁用"),

    ENABLED(1, "启用"),

    PENDING(3, "待审核"),

    DELETED(4, "已删除"),

    ;

    private Integer code;

    private String description;

    public static String getDescription(Integer code) {
        return Arrays.asList(KnowledgeDocumentStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .map(KnowledgeDocumentStatusEnum::getDescription)
                .orElse("未知");
    }

}
