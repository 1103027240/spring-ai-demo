package cn.example.agent.demo.controller;

import cn.example.agent.demo.service.MultiParallelService;
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
@Tag(name = "并行多智能体接口", description = "并行多智能体接口")
@RequestMapping("/multiParallel")
@RestController
public class MultiParallelController {

    @Autowired
    private MultiParallelService multiParallelService;

    @Operation(summary = "客户信息核查对话", description = "客户信息核查对话")
    @PostMapping("/doChat")
    public Map<String, Object> doChat(
            @Parameter(description = "客户ID", required = true, example = "123456")
            @RequestParam(value = "customerId") String customerId) {
        return multiParallelService.doChat(customerId);
    }

}
