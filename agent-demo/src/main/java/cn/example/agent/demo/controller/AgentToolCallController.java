package cn.example.agent.demo.controller;

import cn.example.agent.demo.service.AgentToolCallService;
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
@Tag(name = "智能体工具调用接口", description = "智能体工具调用接口")
@RequestMapping("/agentToolCall")
@RestController
public class AgentToolCallController {

    @Autowired
    private AgentToolCallService agentToolCallService;

    @Operation(summary = "工具调用对话", description = "工具调用对话")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message){
        return agentToolCallService.doChat(message);
    }

}
