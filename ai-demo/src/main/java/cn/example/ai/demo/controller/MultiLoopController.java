package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.MultiLoopService;
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
@Tag(name = "循环多智能体接口", description = "循环多智能体接口")
@RequestMapping("/multiLoop")
@RestController
public class MultiLoopController {

    @Autowired
    private MultiLoopService MultiLoopService;

    @Operation(summary = "客服对话", description = "客服对话")
    @PostMapping("/doChat")
    public Map<String, Object> doChat(
            @Parameter(description = "用户消息") @RequestParam(name = "message") String message,
            @Parameter(description = "用户ID") @RequestParam(name = "userId") Long userId,
            @Parameter(description = "SessionId") @RequestParam(name = "SessionId") String sessionId) {
        return MultiLoopService.doChat(message, userId, sessionId);
    }

}
