package cn.getech.base.demo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author 11030
 */
@Data
@Builder
public class DocumentReviewResumeDto {

    private String instanceId;
    private String decision;  // "APPROVE" 或 "REJECT"
    private String comment;
    private String approver;

}
