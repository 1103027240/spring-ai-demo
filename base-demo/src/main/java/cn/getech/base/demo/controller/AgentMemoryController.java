package cn.getech.base.demo.controller;

import cn.getech.base.demo.service.AgentMemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 11030
 */
@Tag(name = "Agent记忆接口", description = "Agent记忆接口")
@RequestMapping("/agentMemory")
@RestController
public class AgentMemoryController {

    @Autowired
    private AgentMemoryService agentMemoryService;

    @Operation(summary = "Agent长期记忆对话", description = "Agent长期记忆对话")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message,
            @Parameter(description = "userId", required = true, example = "userId")
            @RequestParam(value = "userId") String userId,
            @Parameter(description = "sessionId", required = true, example = "sessionId")
            @RequestParam(value = "sessionId") String sessionId){
        return agentMemoryService.doChat(message, userId, sessionId);
    }

}
