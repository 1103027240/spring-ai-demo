package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.AgentDemoService;
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
@Tag(name = "智能体示例接口", description = "智能体示例接口")
@RequestMapping("/agentDemo")
@RestController
public class AgentDemoController {

    @Autowired
    private AgentDemoService agentDemoService;

    @Operation(summary = "智能体对话", description = "智能体对话")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "message") String message){
        return agentDemoService.doChat(message);
    }

}
