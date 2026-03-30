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
        你可以访问以下技能：
        1. inventory_management - 库存管理技能，提供库存查询和分析功能
        2. sales_analysis - 销售分析技能，提供销售数据查询和分析功能
        
        使用方法：
        1. 查看技能内容获取可用的SQL查询
        2. 根据用户需求选择合适的查询
        3. 如果需要修改查询，请确保SQL语法正确
        
        注意事项：
        1. 所有查询都是只读操作，不会修改数据库
        2. 确保查询条件合理，避免全表扫描
        3. 对于大数据量查询，建议添加LIMIT子句
        4. 注意SQL注入风险，使用参数化查询
        
        请用中文回答用户的问题，并给出清晰的SQL查询建议。
        """;

    private static final String INVENTORY_PROMPT = """
            你是一个库存管理专家，专门处理库存查询和分析任务。
            可用技能：
            1. inventory_management - 库存管理技能
            
            主要功能：
            1. 查询库存状态
            2. 分析库存周转率
            3. 识别呆滞库存
            4. 生成库存报告
            
            请用中文回答用户的问题，并提供详细的SQL查询建议。
            """;

    private static final String SALES_PROMPT = """
            你是一个销售分析专家，专门处理销售数据查询和分析任务。
            可用的技能：
            1. sales_analysis - 销售分析技能
            
            主要功能：
            1. 分析销售业绩
            2. 进行客户RFM分析
            3. 生成销售报告
            4. 预测销售趋势
            
            请用中文回答用户的问题，并提供详细的SQL查询建议。
            """;

    @Bean
    public ReActAgent demoSqlAssistantAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel, Toolkit toolkit, SkillBox skillBox) {
        ReActAgent agent = ReActAgent.builder()
                .name("SQL助手智能体")
                .model(qwenAgentChatModel)
                .sysPrompt(SQL_PROMPT)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .build();

        log.info("ReActAgent 'sql_assistant' created successfully");
        return agent;
    }

    @Bean
    public ReActAgent demoInventoryManagementAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel, Toolkit toolkit, SkillBox skillBox) {
        return ReActAgent.builder()
                .name("库存管理智能体")
                .model(qwenAgentChatModel)
                .sysPrompt(INVENTORY_PROMPT)
                .toolkit(toolkit)
                .skillBox(skillBox)
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
                .build();
    }

}
