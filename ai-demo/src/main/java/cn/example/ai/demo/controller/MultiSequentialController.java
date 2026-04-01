package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.MultiSequentialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * @author 11030
 */
@Tag(name = "顺序多智能体接口", description = "顺序多智能体接口")
@RequestMapping("/multiSequential")
@RestController
public class MultiSequentialController {

    @Autowired
    private MultiSequentialService multiSequentialService;

    @Operation(summary = "退货对话", description = "退货对话")
    @PostMapping("/doChat")
    public Map<String, Object> doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "message") String message) {
        return multiSequentialService.doChat(message);
    }

}
