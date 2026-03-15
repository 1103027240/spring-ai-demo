package cn.getech.base.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 11030
 */
@Data
@Builder
public class DocumentReviewResumeDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String instanceId; // threadId
    private String decision;  // 传入APPROVE或REJECT
    private String comment;
    private String approver;

}
