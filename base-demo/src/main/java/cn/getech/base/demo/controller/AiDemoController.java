package cn.getech.base.demo.controller;

import cn.getech.base.demo.service.AiDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
@Tag(name = "AI Demo测试接口", description = "AI Demo测试相关API")
@RequestMapping("/ai/demo")
@RestController
public class AiDemoController {

    @Autowired
    private AiDemoService demoService;

    @Operation(summary = "通义千问ChatModel同步聊天", description = "通义千问ChatModel同步聊天")
    @GetMapping("/chatModel/qwen")
    public String doChatModelQwen(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return demoService.doChatModelQwen(msg);
    }

    @Operation(summary = "Deepseek ChatModel流式聊天", description = "Deepseek ChatModel流式聊天")
    @GetMapping("/chatModel/deepseek")
    public Flux<String> doChatModelDeepseek(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return demoService.doChatModelDeepseek(msg);
    }

    @Operation(summary = "通义千问ChatClient同步聊天", description = "通义千问ChatClient同步聊天")
    @GetMapping("/chatClient/qwen")
    public String doChatClientQwen(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return demoService.doChatClientQwen(msg);
    }

    @Operation(summary = "Deepseek ChatClient流式聊天", description = "Deepseek ChatClient流式聊天")
    @GetMapping("/chatClient/deepseek")
    public Flux<String> doChatClientDeepseek(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return demoService.doChatClientDeepseek(msg);
    }

}
