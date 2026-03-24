package cn.example.base.demo.controller;

import cn.example.base.demo.service.McpService;
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
@Tag(name = "MCP接口", description = "MCP接口")
@RequestMapping("/mcp")
@RestController
public class McpController {

    @Autowired
    private McpService mcpService;

    @Operation(summary = "AI远程调用百度地图", description = "AI远程调用百度地图")
    @GetMapping("/doChat")
    public Flux<String> doChat(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return mcpService.doChat(msg);
    }

}
