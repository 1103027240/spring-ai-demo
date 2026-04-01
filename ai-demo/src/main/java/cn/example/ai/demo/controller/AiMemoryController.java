package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.AiMemoryService;
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
@Tag(name = "对话记忆接口", description = "向量化接口")
@RequestMapping("/aiMemory")
@RestController
public class AiMemoryController {

    @Autowired
    private AiMemoryService aiMemoryService;

    @Operation(summary = "分层记忆对话", description = "分层记忆对话")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "1加1等于几")
            @RequestParam(value = "msg") String msg,
            @Parameter(description = "用户ID", required = true, example = "123456")
            @RequestParam(value = "conversationId") String conversationId){
        return aiMemoryService.doChat(msg, conversationId);
    }

}
