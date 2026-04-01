package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.MultiRoutingService;
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
@Tag(name = "路由决策多智能体接口", description = "路由决策多智能体接口")
@RequestMapping("/multiRouting")
@RestController
public class MultiRoutingController {

    @Autowired
    private MultiRoutingService multiRoutingService;

    @Operation(summary = "代码搜索", description = "代码搜索")
    @PostMapping("/doChatSimple")
    public Map<String, Object> doChatSimple(@Parameter(description = "用户消息") @RequestParam(name = "message") String message) {
        return multiRoutingService.doChatSimple(message);
    }

    @Operation(summary = "代码搜索（工作流）", description = "代码搜索（工作流）")
    @PostMapping("/doChatGraph")
    public Map<String, Object> doChatGraph(@Parameter(description = "用户消息") @RequestParam(name = "message") String message) {
        return multiRoutingService.doChatGraph(message);
    }

}
