package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.MultiSkillService;
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
@Tag(name = "技能包智能体接口", description = "技能包智能体接口")
@RequestMapping("/multiSkill")
@RestController
public class MultiSkillController {

    @Autowired
    private MultiSkillService multiSkillService;

    @Operation(summary = "Sql技能包使用", description = "Sql技能包使用")
    @PostMapping("/doChatSqlAssistant")
    public Map<String, Object> doChatSqlAssistant(@Parameter(description = "用户消息") @RequestParam(name = "message") String message) {
        return multiSkillService.doChatSqlAssistant(message);
    }

    @Operation(summary = "库存管理技能包使用", description = "库存管理技能包使用")
    @PostMapping("/doChatInventoryManagement")
    public Map<String, Object> doChatInventoryManagement(@Parameter(description = "用户消息") @RequestParam(name = "message") String message) {
        return multiSkillService.doChatInventoryManagement(message);
    }

    @Operation(summary = "销售分析技能包使用", description = "销售分析技能包使用")
    @PostMapping("/doyChatSalesAnalysis")
    public Map<String, Object> doyChatSalesAnalysis(@Parameter(description = "用户消息") @RequestParam(name = "message") String message) {
        return multiSkillService.doyChatSalesAnalysis(message);
    }

}
