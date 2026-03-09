package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.DocumentReviewDto;
import cn.getech.base.demo.dto.DocumentReviewResumeDto;
import java.util.Map;

/**
 * @author 11030
 */
public interface DocumentReviewService {

    Map<String, Object> startWorkflow(DocumentReviewDto dto);

    Map<String, Object> resumeWorkflow(DocumentReviewResumeDto dto);

}
