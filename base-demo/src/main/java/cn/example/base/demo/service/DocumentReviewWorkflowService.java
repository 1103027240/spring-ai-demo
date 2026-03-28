package cn.example.base.demo.service;

import cn.example.base.demo.param.dto.DocumentReviewDto;
import cn.example.base.demo.param.dto.DocumentReviewResumeDto;
import java.util.Map;

/**
 * @author 11030
 */
public interface DocumentReviewWorkflowService {

    Map<String, Object> startWorkflow(DocumentReviewDto dto);

    Map<String, Object> resumeWorkflow(DocumentReviewResumeDto dto);

}
