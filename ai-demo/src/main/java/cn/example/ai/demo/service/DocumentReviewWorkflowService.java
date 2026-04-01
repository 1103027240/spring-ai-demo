package cn.example.ai.demo.service;

import cn.example.ai.demo.param.dto.DocumentReviewDto;
import cn.example.ai.demo.param.dto.DocumentReviewResumeDto;
import java.util.Map;

/**
 * @author 11030
 */
public interface DocumentReviewWorkflowService {

    Map<String, Object> startWorkflow(DocumentReviewDto dto);

    Map<String, Object> resumeWorkflow(DocumentReviewResumeDto dto);

}
