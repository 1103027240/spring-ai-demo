package cn.getech.base.demo.controller;

import cn.getech.base.demo.service.AgentDemoService;
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
@Tag(name = "Agent Demo测试接口", description = "Agent Demo测试相关API")
@RequestMapping("/agent/demo")
@RestController
public class AgentDemoController {

    @Autowired
    private AgentDemoService agentDemoService;

    @Operation(summary = "通义千问ChatModel同步聊天", description = "通义千问ChatModel同步聊天")
    @GetMapping("/chatModel/qwen")
    public String doChatModelQwen(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return agentDemoService.doChatModelQwen(msg);
    }

}
