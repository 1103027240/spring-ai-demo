package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum DocumentReviewNodeEnum {

    CONTENT_ANALYSIS("内容分析"),

    COMPLIANCE_CHECK("合规检查"),

    RISK_ASSESSMENT("风险评估"),

    HUMAN_APPROVAL("人工审批"),

    APPROVE_PROCESSING("通过"),

    REJECT_PROCESSING("拒绝"),

    FINAL_REPORT("最终报告"),

    ;

    private String name;

}

