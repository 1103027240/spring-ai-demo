package cn.example.mcp.client.demo.controller;

import cn.example.mcp.client.demo.service.McpDemoService;
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
@Tag(name = "Demo测试接口", description = "Demo测试相关API")
@RequestMapping("/demo")
@RestController
public class McpDemoController {

    @Autowired
    private McpDemoService mcpDemoService;

    @Operation(summary = "SSE调用MCP服务", description = "SSE调用MCP服务")
    @GetMapping("/doChat")
    public String doChat(
            @Parameter(description = "用户消息内容", required = true, example = "查询华为mate50手机商品信息")
            @RequestParam(value = "msg") String msg){
        return mcpDemoService.doChat(msg);
    }

}
