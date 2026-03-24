package cn.example.base.demo.controller;

import cn.example.base.demo.service.MultiAgentDemoService;
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
@Tag(name = "多智能体示例接口", description = "多智能体示例接口")
@RequestMapping("/multiAgentDemo")
@RestController
public class MultiAgentDemoController {

    @Autowired
    private MultiAgentDemoService multiAgentDemoService;

    @Operation(summary = "多智能体对话", description = "多智能体对话")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message){
        return multiAgentDemoService.doChat(message);
    }

}
