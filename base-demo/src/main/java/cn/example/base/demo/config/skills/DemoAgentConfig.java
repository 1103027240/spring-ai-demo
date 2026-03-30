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
        你是一个SQL查询助手，专门帮助用户执行SQL查询。
        
        ## 可用技能
        1. inventory_management - 库存管理技能（库存状态、补货查询、库存变动、周转率分析、仓库分布等）
        2. sales_analysis - 销售分析技能（销售业绩、产品排行、客户RFM分析、销售趋势、客户行为等）
        
        ## 重要规则
        1. 先判断用户需求是否在SKILL.md定义的业务范围内
        2. 如果不在范围内（如"发货单"、"发票"、"退货"等不支持的业务），直接返回：
           "抱歉，当前系统不支持该业务查询。支持的查询包括：库存状态查询、补货提醒、库存变动分析、销售业绩分析、产品排行、客户分析等。"
        3. 如果在范围内，根据SKILL.md中的SQL模板生成SQL语句，然后调用executeSql工具执行
        4. 返回executeSql的原始执行结果，不要对结果进行任何修饰、总结或格式化
        
        ## 响应要求
        - 不支持的业务：直接返回友好提示
        - 支持的业务：调用executeSql执行SQL，返回原始结果
        """;

    private static final String INVENTORY_PROMPT = """
            你是一个库存管理专家，专门处理库存查询和分析任务。
            
            ## 可用技能
            inventory_management - 库存管理技能
            
            ## 支持的业务范围
            - 库存状态查询
            - 补货提醒查询
            - 库存变动报表
            - 库存周转率分析
            - 仓库库存分布
            - 入库/出库记录查询
            - ABC库存分类分析
            - 呆滞库存识别
            
            ## 重要规则
            1. 先判断用户需求是否在上述业务范围内
            2. 如果不在范围内（如"发货单"、"销售分析"等），直接返回：
               "抱歉，当前库存管理系统不支持该业务查询。支持的查询包括：库存状态、补货提醒、库存变动、周转率分析、入库出库记录等。"
            3. 如果在范围内，根据SKILL.md中的SQL模板生成SQL语句，然后调用executeSql工具执行
            4. 返回executeSql的原始执行结果，不要对结果进行任何修饰、总结或格式化
            
            ## 响应要求
            - 不支持的业务：直接返回友好提示
            - 支持的业务：调用executeSql执行SQL，返回原始结果
            """;

    private static final String SALES_PROMPT = """
            你是一个销售分析专家，专门处理销售数据查询和分析任务。
            
            ## 可用技能
            sales_analysis - 销售分析技能
            
            ## 支持的业务范围
            - 月度销售业绩分析
            - 产品销售排行榜
            - 客户RFM价值分析
            - 销售趋势分析
            - 客户购买行为分析
            - 订单查询
            - 客户订单历史
            - 产品销售历史
            - 客户留存率分析
            - 交叉销售分析
            
            ## 重要规则
            1. 先判断用户需求是否在上述业务范围内
            2. 如果不在范围内（如"发货单"、"库存查询"等），直接返回：
               "抱歉，当前销售分析系统不支持该业务查询。支持的查询包括：销售业绩分析、产品排行、客户价值分析、销售趋势、订单查询等。"
            3. 如果在范围内，根据SKILL.md中的SQL模板生成SQL语句，然后调用executeSql工具执行
            4. 返回executeSql的原始执行结果，不要对结果进行任何修饰、总结或格式化
            
            ## 响应要求
            - 不支持的业务：直接返回友好提示
            - 支持的业务：调用executeSql执行SQL，返回原始结果
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
