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

    CONTENT_ANALYSIS("content_analysis", "内容分析"),

    COMPLIANCE_CHECK("compliance_check", "合规检查"),

    RISK_ASSESSMENT("risk_assessment", "风险评估"),

    HUMAN_APPROVAL("human_approval", "人工审批"),

    APPROVE_PROCESSING("approve_processing", "通过"),

    REJECT_PROCESSING("reject_processing", "拒绝"),

    FINAL_REPORT("final_report", "最终报告"),

    ;

    private String id;

    private String text;

}

