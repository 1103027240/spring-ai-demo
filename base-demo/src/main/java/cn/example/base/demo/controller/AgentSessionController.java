package cn.example.base.demo.controller;

import cn.example.base.demo.service.AgentSessionService;
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
@Tag(name = "智能体会话接口", description = "智能体会话接口")
@RequestMapping("/agentSession")
@RestController
public class AgentSessionController {

    @Autowired
    private AgentSessionService agentSessionService;

    @Operation(summary = "Json会话", description = "Json会话")
    @GetMapping("/doChatJsonSession")
    public String doChatJsonSession(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message,
            @Parameter(description = "sessionId", required = true, example = "sessionId")
            @RequestParam(value = "sessionId") String sessionId){
        return agentSessionService.doChatJsonSession(message, sessionId);
    }

    @Operation(summary = "Mysql会话", description = "Mysql会话")
    @GetMapping("/doChatMysqlSession")
    public String doChatMysqlSession(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message,
            @Parameter(description = "sessionId", required = true, example = "sessionId")
            @RequestParam(value = "sessionId") String sessionId){
        return agentSessionService.doChatMysqlSession(message, sessionId);
    }

    @Operation(summary = "SessionManager会话", description = "SessionManager会话")
    @GetMapping("/doChatSessionManager")
    public String doChatSessionManager(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message,
            @Parameter(description = "sessionId", required = true, example = "sessionId")
            @RequestParam(value = "sessionId") String sessionId){
        return agentSessionService.doChatSessionManager(message, sessionId);
    }

}
