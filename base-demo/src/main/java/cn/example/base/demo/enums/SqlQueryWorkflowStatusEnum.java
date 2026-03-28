package cn.example.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SqlQueryWorkflowStatusEnum {

    STARTED("STARTED"),

    FAILED("FAILED"),

    PROCESSING("PROCESSING"),

    COMPLETED("COMPLETED"),

    ERROR("ERROR"),

    CRITICAL_ERROR("CRITICAL_ERROR"),

    ;

    private String id;

}
