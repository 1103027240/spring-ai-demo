package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 工作流流程状态
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ApprovalStatusEnum {

    WAITING("WAIT", "待审批"),

    PENDING("PENDING", "审批中"),

    COMPLETED("COMPLETED", "已完成"),

    ;

    private String id;
    private String description;

}
