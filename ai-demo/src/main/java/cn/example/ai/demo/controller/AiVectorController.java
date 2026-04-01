package cn.example.ai.demo.controller;

import cn.example.ai.demo.entity.LongTermChatMemory;
import cn.example.ai.demo.service.AiVectorService;
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
@Tag(name = "向量化接口", description = "向量化接口")
@RequestMapping("/aiVector")
@RestController
public class AiVectorController {

    @Autowired
    private AiVectorService aiVectorService;

    @Operation(summary = "新增向量数据", description = "新增向量数据")
    @GetMapping("/addVector")
    public void addVector(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        aiVectorService.addVector(msg);
    }

    @Operation(summary = "查询向量数据", description = "查询向量数据")
    @GetMapping("/getVector")
    public List<LongTermChatMemory> getVector(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return aiVectorService.getVector(msg);
    }

}
