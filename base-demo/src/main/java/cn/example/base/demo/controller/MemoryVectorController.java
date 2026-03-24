package cn.example.base.demo.controller;

import cn.example.base.demo.entity.LongTermChatMemory;
import cn.example.base.demo.service.MemoryVectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author 11030
 */
@Tag(name = "对话记忆及向量化接口", description = "对话记忆及向量化接口")
@RequestMapping("/memoryVector")
@RestController
public class MemoryVectorController {

    @Autowired
    private MemoryVectorService memoryVectorService;

    @Operation(summary = "分层记忆对话", description = "分层记忆对话")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "1加1等于几")
            @RequestParam(value = "msg") String msg,
            @Parameter(description = "用户ID", required = true, example = "123456")
            @RequestParam(value = "conversationId") String conversationId){
        return memoryVectorService.doChat(msg, conversationId);
    }

    @Operation(summary = "新增向量数据", description = "新增向量数据")
    @GetMapping("/addMemory")
    public void addMemory(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        memoryVectorService.addMemory(msg);
    }

    @Operation(summary = "查询向量数据", description = "查询向量数据")
    @GetMapping("/getMemory")
    public List<LongTermChatMemory> getMemory(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return memoryVectorService.getMemory(msg);
    }

}
