package cn.example.agent.demo.config.skills;

import cn.example.agent.demo.tools.SqlQueryTools;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InventorySalesAgentConfig {

    private static final String SQL_PROMPT = """
        你是SQL查询助手。
        可用技能：
        - inventory_management：库存管理（库存状态、补货、周转率、入库出库等）
        - sales_analysis：销售分析（销售业绩、产品排行、客户分析、订单查询等）
       
        可用工具：
        - executeSql：执行SQL查询
        
        ## 规则
        1. 匹配技能 → 查看SKILL.md获取SQL → 调用executeSql工具
        2. 不匹配任何技能 → 直接返回：{"success":false,"error":"抱歉，当前系统不支持该业务查询。"}
        3. 只能使用上面匹配技能中SKILL.md表名和字段，禁止使用其他表名和字段
        4. 必须调用executeSql工具执行SQL，executeSql返回JSON字符串，直接输出该JSON字符串，禁止转换成markdown表格，禁止添加任何文字
        5. 所有SQL必须加 LIMIT 10
        """;

    private static final String INVENTORY_PROMPT = """
        你是库存管理专家。
        可用技能：
        - inventory_management：库存管理（库存状态、补货、周转率、入库出库等）
        
        可用工具：
        - executeSql：执行SQL查询
        
        ## 规则
        1. 匹配业务 → 查看SKILL.md获取SQL → 调用executeSql工具
        2. 不匹配 → 直接返回：{"success":false,"error":"抱歉，当前库存系统不支持该业务查询。"}
        3. 只能使用上面匹配技能中SKILL.md表名和字段，禁止使用其他表名和字段
        4. 必须调用executeSql工具执行SQL，executeSql返回JSON字符串，直接输出该JSON字符串，禁止转换成markdown表格，禁止添加任何文字
        5. 所有SQL必须加 LIMIT 10
        """;

    private static final String SALES_PROMPT = """
        你是销售分析专家。
        可用技能：
        - sales_analysis：销售分析（销售业绩、产品排行、客户分析、订单查询等）
        
       可用工具：
        - executeSql：执行SQL查询
        
        ## 规则
        1. 匹配业务 → 查看SKILL.md获取SQL → 调用executeSql工具
        2. 不匹配 → 直接返回：{"success":false,"error":"抱歉，当前销售系统不支持该业务查询。"}
        3. 只能使用上面匹配技能中SKILL.md表名和字段，禁止使用其他表名和字段
        4. 必须调用executeSql工具执行SQL，executeSql返回JSON字符串，直接输出该JSON字符串，禁止转换成markdown表格，禁止添加任何文字
        5. 所有SQL必须加 LIMIT 10
        """;

    @Bean
    public ReactAgent demoSqlAssistantAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                            @Qualifier("skillsAgentHook") SkillsAgentHook skillsAgentHook) {
        return ReactAgent.builder()
                .name("SQL助手智能体")
                .model(qwenChatModel)
                .systemPrompt(SQL_PROMPT)
                .instruction("用户问题：{message}")
                .methodTools(new SqlQueryTools())
                .hooks(skillsAgentHook, ModelCallLimitHook.builder().runLimit(5).build())
                .build();
    }

    @Bean
    public ReactAgent demoInventoryManagementAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                                   @Qualifier("skillsAgentHook") SkillsAgentHook skillsAgentHook) {
        return ReactAgent.builder()
                .name("库存管理智能体")
                .model(qwenChatModel)
                .systemPrompt(INVENTORY_PROMPT)
                .instruction("用户问题：{message}")
                .methodTools(new SqlQueryTools())
                .hooks(skillsAgentHook, ModelCallLimitHook.builder().runLimit(5).build())
                .build();
    }

    @Bean
    public ReactAgent demoSalesAnalysisAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                             @Qualifier("skillsAgentHook") SkillsAgentHook skillsAgentHook) {
        return ReactAgent.builder()
                .name("销售分析智能体")
                .model(qwenChatModel)
                .systemPrompt(SALES_PROMPT)
                .instruction("用户问题：{message}")
                .methodTools(new SqlQueryTools())
                .hooks(skillsAgentHook, ModelCallLimitHook.builder().runLimit(5).build())
                .build();
    }

}
