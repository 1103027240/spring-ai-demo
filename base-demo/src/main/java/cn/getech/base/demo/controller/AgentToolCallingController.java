package cn.getech.base.demo.controller;

import cn.getech.base.demo.service.AgentToolCallingService;
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
@Tag(name = "Agent工具调用接口", description = "Agent工具调用接口")
@RequestMapping("/agentToolCalling")
@RestController
public class AgentToolCallingController {

    @Autowired
    private AgentToolCallingService agentToolCallingService;

    @Operation(summary = "工具组对话", description = "工具组对话")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message){
        return agentToolCallingService.doChat(message);
    }

}
