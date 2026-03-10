package cn.getech.base.demo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author 11030
 */
@Data
@Builder
public class DocumentReviewResumeDto {

    private String instanceId; // threadId
    private String decision;  // 传入APPROVE或REJECT
    private String comment;
    private String approver;

}
