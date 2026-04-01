package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.AiDemoService;
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
@Tag(name = "AI示例接口", description = "AI示例接口")
@RequestMapping("/aiDemo")
@RestController
public class AiDemoController {

    @Autowired
    private AiDemoService aiDemoService;

    @Operation(summary = "通义千问ChatModel同步对话", description = "通义千问ChatModel同步对话")
    @GetMapping("/doChatModelQwen")
    public String doChatModelQwen(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return aiDemoService.doChatModelQwen(msg);
    }

    @Operation(summary = "Deepseek ChatModel流式对话", description = "Deepseek ChatModel流式对话")
    @GetMapping("/doChatModelDeepseek")
    public Flux<String> doChatModelDeepseek(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return aiDemoService.doChatModelDeepseek(msg);
    }

    @Operation(summary = "通义千问ChatClient同步对话", description = "通义千问ChatClient同步对话")
    @GetMapping("/doChatClientQwen")
    public String doChatClientQwen(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return aiDemoService.doChatClientQwen(msg);
    }

    @Operation(summary = "Deepseek ChatClient流式对话", description = "Deepseek ChatClient流式对话")
    @GetMapping("/doChatClientDeepseek")
    public Flux<String> doChatClientDeepseek(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return aiDemoService.doChatClientDeepseek(msg);
    }

}
