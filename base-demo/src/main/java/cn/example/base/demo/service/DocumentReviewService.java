package cn.example.base.demo.service;

import cn.example.base.demo.dto.DocumentReviewDto;
import cn.example.base.demo.dto.DocumentReviewResumeDto;
import java.util.Map;

/**
 * @author 11030
 */
public interface DocumentReviewService {

    Map<String, Object> startWorkflow(DocumentReviewDto dto);

    Map<String, Object> resumeWorkflow(DocumentReviewResumeDto dto);

}
