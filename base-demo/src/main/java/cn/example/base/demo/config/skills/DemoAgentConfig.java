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
public class DemoAgentConfig {

    private static final String SQL_PROMPT = """
        你是SQL查询助手。只能访问以下技能：
        - inventory_management：库存管理（库存状态、补货、周转率、入库出库等）
        - sales_analysis：销售分析（销售业绩、产品排行、客户分析、订单查询等）
        
        ## 规则
        1. 匹配技能 → 查看SKILL.md意图映射表 → 生成SQL → 调用executeSql → 返回原始结果
        2. 不匹配任何技能 → 返回："抱歉，当前系统不支持该业务查询。"
        
        ## 示例
        用户: 查询库存状态
        动作: 匹配inventory_management → 意图映射"库存状态" → 生成SQL → 返回结果
        
        用户: 查询销售排行
        动作: 匹配sales_analysis → 意图映射"产品排行" → 生成SQL → 返回结果
        
        用户: 查询发货单
        动作: 不匹配任何技能 → 返回"抱歉，当前系统不支持该业务查询。"
        
        用户: 看看有哪些产品缺货
        动作: 匹配inventory_management → 意图映射"补货提醒" → 生成SQL → 返回结果
        
        用户: 最近销售怎么样
        动作: 匹配sales_analysis → 意图映射"销售业绩" → 生成SQL → 返回结果
        """;

    private static final String INVENTORY_PROMPT = """
        你是库存管理专家。只能访问 inventory_management 技能。
        
        ## 支持业务
        库存状态、补货提醒、库存变动、周转率、仓库分布、入库、出库、ABC分类、呆滞库存
        
        ## 规则
        1. 匹配业务 → 查看SKILL.md意图映射表 → 生成SQL → 调用executeSql → 返回原始结果
        2. 不匹配 → 返回："抱歉，当前库存系统不支持该业务查询。支持：库存状态、补货提醒、入库出库等。"
        
        ## 示例
        用户: 查看库存
        动作: 意图映射"库存状态" → 生成SQL → 返回结果
        
        用户: 哪些产品需要补货
        动作: 意图映射"补货提醒" → 生成SQL → 返回结果
        
        用户: 看看入库记录
        动作: 意图映射"入库记录" → 生成SQL → 返回结果
        
        用户: 哪些产品卖不动
        动作: 意图映射"呆滞库存" → 生成SQL → 返回结果
        
        用户: 查询销售排行
        动作: 不匹配inventory_management → 返回"抱歉，当前库存系统不支持该业务查询。"
        """;

    private static final String SALES_PROMPT = """
        你是销售分析专家。只能访问 sales_analysis 技能。
        
        ## 支持业务
        销售业绩、产品排行、客户分析(RFM)、销售趋势、订单查询、客户购买行为、留存率、交叉销售
        
        ## 规则
        1. 匹配业务 → 查看SKILL.md意图映射表 → 生成SQL → 调用executeSql → 返回原始结果
        2. 不匹配 → 返回："抱歉，当前销售系统不支持该业务查询。支持：销售业绩、产品排行、客户分析、订单查询等。"
        
        ## 示例
        用户: 查看销售情况
        动作: 意图映射"销售业绩" → 生成SQL → 返回结果
        
        用户: 产品销量排名
        动作: 意图映射"产品排行" → 生成SQL → 返回结果
        
        用户: 分析客户价值
        动作: 意图映射"客户分析" → 生成SQL → 返回结果
        
        用户: 查看订单
        动作: 意图映射"订单列表" → 生成SQL → 返回结果
        
        用户: 查询库存
        动作: 不匹配sales_analysis → 返回"抱歉，当前销售系统不支持该业务查询。"
        """;

    @Bean
    public ReActAgent demoSqlAssistantAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel, Toolkit toolkit, SkillBox skillBox) {
        return ReActAgent.builder()
                .name("SQL助手智能体")
                .model(qwenAgentChatModel)
                .sysPrompt(SQL_PROMPT)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .maxIters(1)
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
                .maxIters(1)
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
                .maxIters(1)
                .build();
    }

}
