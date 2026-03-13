package cn.getech.base.demo.controller;

import cn.getech.base.demo.dto.MessageDocumentVO;
import cn.getech.base.demo.service.WorkflowExecutionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * @author 11030
 */
@Tag(name = "售后客服工作流接口", description = "售后客服工作流相关API")
@RequestMapping("/customerService")
@RestController
public class CustomerServiceController {

    @Autowired
    private WorkflowExecutionService workflowExecutionService;

    @PostMapping("/chat")
    @Operation(summary = "客服对话", description = "处理用户咨询并返回AI回复")
    public Map<String, Object> chat(
            @Parameter(description = "用户消息") @RequestParam(name = "message") String message,
            @Parameter(description = "用户ID") @RequestParam(name = "userId") Long userId,
            @Parameter(description = "用户名") @RequestParam(name = "userName") String userName) {
        return workflowExecutionService.executeWorkflow(message, userId, userName);
    }

    @PostMapping("/history")
    @Operation(summary = "获取会话历史", description = "获取指定用户会话聊天历史")
    public Page<MessageDocumentVO> pageChatHistory(
            @Parameter(description = "用户ID") @RequestParam(name = "userId") Long userId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") String currentPage,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") String pageSize) {
        return workflowExecutionService.pageChatHistory(userId, currentPage, pageSize);
    }

}
