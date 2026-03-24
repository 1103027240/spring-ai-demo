package cn.example.base.demo.controller;

import cn.example.base.demo.service.AgentMcpService;
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
@Tag(name = "智能体MCP接口", description = "智能体MCP接口")
@RequestMapping("/agentMcp")
@RestController
public class AgentMcpController {

    @Autowired
    private AgentMcpService agentMcpService;

    @Operation(summary = "智能体远程调用百度地图", description = "智能体远程调用百度地图")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message){
        return agentMcpService.doChat(message);
    }

}
