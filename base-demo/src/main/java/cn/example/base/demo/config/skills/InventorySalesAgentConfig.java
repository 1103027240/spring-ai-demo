package cn.example.base.demo.config.skills;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InventorySalesAgentConfig {

    private static final String SQL_PROMPT = """
        你是SQL查询助手。只能访问以下技能：
        - inventory_management：库存管理（库存状态、补货、周转率、入库出库等）
        - sales_analysis：销售分析（销售业绩、产品排行、客户分析、订单查询等）
        
        ## 可用表名
        inventory_management技能：products、inventory、stock_in、stock_out
        sales_analysis技能：customers、orders、order_items、products
        
        ## 规则
        1. 匹配技能 → 查看SKILL.md意图映射表 → 获取SQL → 调用executeSql工具
        2. 不匹配任何技能 → 直接返回：{"success":false,"error":"抱歉，当前系统不支持该业务查询。"}
        3. 只能使用上述表名，禁止使用其他表名
        4. 必须调用executeSql工具执行SQL
        5. 所有SQL必须加 LIMIT 10
        6. executeSql返回JSON字符串，直接输出该JSON字符串，禁止转换成markdown表格，禁止添加任何文字
        """;

    private static final String INVENTORY_PROMPT = """
        你是库存管理专家。只能访问 inventory_management 技能。
        
        ## 可用表名
        products、inventory、stock_in、stock_out
        
        ## 支持业务
        库存状态、补货提醒、库存变动、周转率、仓库分布、入库、出库、ABC分类、呆滞库存
        
        ## 规则
        1. 匹配业务 → 查看SKILL.md获取SQL → 调用executeSql工具
        2. 不匹配 → 直接返回：{"success":false,"error":"抱歉，当前库存系统不支持该业务查询。"}
        3. 只能使用上述表名，禁止使用其他表名
        4. 必须调用executeSql工具执行SQL
        5. 所有SQL必须加 LIMIT 10
        6. executeSql返回JSON字符串，直接输出该JSON字符串，禁止转换成markdown表格，禁止添加任何文字
        """;

    private static final String SALES_PROMPT = """
        你是销售分析专家。只能访问 sales_analysis 技能。
        
        ## 可用表名
        customers、orders、order_items、products
        
        ## 支持业务
        销售业绩、产品排行、客户分析(RFM)、销售趋势、订单查询、客户购买行为、留存率、交叉销售
        
        ## 规则
        1. 匹配业务 → 查看SKILL.md获取SQL → 调用executeSql工具
        2. 不匹配 → 直接返回：{"success":false,"error":"抱歉，当前销售系统不支持该业务查询。"}
        3. 只能使用上述表名，禁止使用其他表名如sales
        4. 必须调用executeSql工具执行SQL
        5. 所有SQL必须加 LIMIT 10
        6. executeSql返回JSON字符串，直接输出该JSON字符串，禁止转换成markdown表格，禁止添加任何文字
        """;

    @Bean
    public ReActAgent demoSqlAssistantAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel, Toolkit toolkit, SkillBox skillBox) {
        return ReActAgent.builder()
                .name("SQL助手智能体")
                .model(qwenAgentChatModel)
                .sysPrompt(SQL_PROMPT)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .maxIters(2)
                .build();
    }

    @Bean
    public ReActAgent demoInventoryManagementAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel, Toolkit toolkit, SkillBox skillBox) {
        return ReActAgent.builder()
                .name("库存管理智能体")
                .model(qwenAgentChatModel)
                .sysPrompt(INVENTORY_PROMPT)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .maxIters(2)
                .build();
    }

    @Bean
    public ReActAgent demoSalesAnalysisAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel, Toolkit toolkit, SkillBox skillBox) {
        return ReActAgent.builder()
                .name("销售分析智能体")
                .model(qwenAgentChatModel)
                .sysPrompt(SALES_PROMPT)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .maxIters(2)
                .build();
    }

}
