package cn.example.agent.demo.controller;

import cn.example.agent.demo.service.MultiA2AService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * @author 11030
 */
@Tag(name = "A2A多智能体接口", description = "A2A多智能体接口")
@RequestMapping("/multiA2a")
@RestController
public class MultiA2AController {

    @Autowired
    private MultiA2AService multiA2AService;

    @Operation(summary = "A2A对话", description = "A2A对话")
    @PostMapping("/doChat")
    public Map<String, Object> doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "message") String message) {
        return multiA2AService.doChat(message);
    }

}
