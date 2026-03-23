package cn.getech.base.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 11030
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DocumentReviewResumeDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String instanceId; // threadId

    private String decision;  // 传入APPROVE或REJECT

    private String comment;

    private String approver;

}
