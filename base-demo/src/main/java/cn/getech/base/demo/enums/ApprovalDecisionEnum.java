package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 审批决策
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ApprovalDecisionEnum {

    WAITING("WAIT", "待审批"),

    PENDING("PENDING", "审批中"),

    APPROVE("APPROVE", "通过"),

    REJECT("REJECT", "拒绝"),

    ;

    private String id;

    private String description;

}
