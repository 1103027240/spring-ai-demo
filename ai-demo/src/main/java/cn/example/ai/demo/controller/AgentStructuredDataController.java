package cn.example.ai.demo.controller;

import cn.example.ai.demo.param.vo.ContactInfoVO;
import cn.example.ai.demo.service.AgentStructuredDataService;
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
@Tag(name = "智能体结构化输出接口", description = "智能体结构化输出接口")
@RequestMapping("/agentStructuredData")
@RestController
public class AgentStructuredDataController {

    @Autowired
    private AgentStructuredDataService agentStructuredDataService;

    @Operation(summary = "结构化输出对话", description = "结构化输出对话")
    @GetMapping("/doChat")
    public ContactInfoVO doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String message){
        return agentStructuredDataService.doChat(message);
    }

}
