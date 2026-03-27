package cn.example.base.demo.controller;

import cn.example.base.demo.dto.DocumentReviewDto;
import cn.example.base.demo.dto.DocumentReviewResumeDto;
import cn.example.base.demo.service.DocumentReviewWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * @author 11030
 */
@Tag(name = "文档审批工作流接口", description = "文档审批工作流接口")
@RequestMapping("/documentReview")
@RestController
public class DocumentReviewController {

    @Autowired
    private DocumentReviewWorkflowService documentReviewWorkflowService;

    @Operation(summary = "启动工作流实例", description = "启动工作流实例")
    @PostMapping("/start")
    public Map<String, Object> start(@Validated @RequestBody DocumentReviewDto dto) {
        return documentReviewWorkflowService.startWorkflow(dto);
    }

    @Operation(summary = "恢复中断工作流实例", description = "恢复中断工作流实例")
    @PostMapping("/resume")
    public Map<String, Object> resume(@Validated @RequestBody DocumentReviewResumeDto dto) {
        return documentReviewWorkflowService.resumeWorkflow(dto);
    }

}
