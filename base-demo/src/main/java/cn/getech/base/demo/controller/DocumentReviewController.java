package cn.getech.base.demo.controller;

import cn.getech.base.demo.dto.DocumentReviewDto;
import cn.getech.base.demo.dto.DocumentReviewResumeDto;
import cn.getech.base.demo.service.DocumentReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * @author 11030
 */
@Tag(name = "文档审批工作流接口", description = "文档审批工作流相关API")
@RequestMapping("/documentReview")
@RestController
public class DocumentReviewController {

    @Autowired
    private DocumentReviewService documentReviewService;

    @Operation(summary = "启动工作流实例", description = "启动工作流实例")
    @PostMapping("/start")
    public Map<String, Object> startWorkflow(@RequestBody DocumentReviewDto dto) {
        return documentReviewService.startWorkflow(dto);
    }

    @Operation(summary = "恢复中断工作流实例", description = "恢复中断工作流实例")
    @PostMapping("/resume")
    public Map<String, Object> resumeWorkflow(@RequestBody DocumentReviewResumeDto dto) {
        return documentReviewService.resumeWorkflow(dto);
    }

}
