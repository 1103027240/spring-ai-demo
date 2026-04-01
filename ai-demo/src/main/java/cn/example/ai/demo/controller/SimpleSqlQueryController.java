package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.SimpleSqlQueryWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * @author 11030
 */
@Tag(name = "数据查询工作流接口", description = "数据查询工作流接口")
@RequestMapping("/simpleSqlQuery")
@RestController
public class SimpleSqlQueryController {

    @Autowired
    private SimpleSqlQueryWorkflowService simpleSqlQueryWorkflowService;

    @PostMapping("/doChat")
    @Operation(summary = "客服对话", description = "处理用户咨询并返回AI回复")
    public Map<String, Object> doChat(
            @Parameter(description = "用户消息") @RequestParam(name = "message") String message) {
        return simpleSqlQueryWorkflowService.executeWorkflow(message);
    }

}
