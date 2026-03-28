package cn.example.base.demo.agent;

import cn.example.base.demo.tools.SqlQueryTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 调用QueryTools中nlToSql（通过智能体调用太慢）
 */
@Slf4j
@Component
public class SqlQueryAgent {

    @Getter
    private ReActAgent reactAgent;

    @Autowired
    public SqlQueryAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel,
                         @Qualifier("sqlQueryTools") SqlQueryTools sqlQueryTools) {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(sqlQueryTools);

        this.reactAgent = ReActAgent.builder()
                .name("SQL查询智能体")
                .description("自然语言转SQL查询智能体")
                .sysPrompt("""
                    你是一个专业的SQL查询智能体。你的职责是将用户的自然语言查询请求转换为准确的SQL查询语句。
                    重要：你只能使用 nlToSql 工具将自然语言转换为SQL，不要调用其他工具。
                    
                    数据库表结构：
                    1. 订单表(t_order): id, order_no, customer_id, customer_name, product_id, product_name, 
                        quantity, unit_price, total_amount, status, create_time, update_time
                    2. 客户表(t_customer): id, customer_no, name, email, phone, customer_type, credit_level, 
                        membership_level, total_orders, total_spent, create_time
                    3. 商品表(t_product): id, product_no, name, category, brand, unit_price, cost_price, status
                    4. 库存表(t_inventory): id, product_id, product_name, sku_code, total_stock, available_stock
                    5. 支付表(t_payment): id, payment_no, order_no, amount, payment_method, status, create_time
                    
                    处理规则：
                    1. queryType只允许SELECT，禁止INSERT/UPDATE/DELETE
                    2. 必须包含LIMIT限制结果集大小
                    3. 对敏感字段进行脱敏处理
                    4. 如果工具调用成功，success为true，error为""
                    5、如果工具调用失败，success为false，error为工具调用返回的失败原因，如果失败原因为空，请自己组织失败原因，限制在60字以内
                    
                    响应格式必须是JSON：
                    {
                        "success": true/false,
                        "queryType": "SELECT",
                        "originalQuery": "原始查询",
                        "generatedSql": "生成的SQL",
                        "error": "失败原因"
                    }
                    """)
                .model(qwenAgentChatModel)
                .toolkit(toolkit)
                .maxIters(5)
                .build();
    }

    public Msg call(Msg msg) {
        long startTime = System.currentTimeMillis();

        try {
            String agentResponse = this.reactAgent.call(msg).block().getTextContent();
            long duration = System.currentTimeMillis() - startTime;
            log.info("SQL查询智能体处理完成，耗时: {}ms", duration);

            return Msg.builder()
                    .name("SQL查询智能体")
                    .role(MsgRole.ASSISTANT)
                    .textContent(agentResponse)
                    .metadata(Map.of(
                            "duration", duration,
                            "success", true))
                    .build();
        } catch (Exception e) {
            log.error("SQL查询智能体处理失败", e);
            long duration = System.currentTimeMillis() - startTime;

            String errorResponse = String.format("""
                {
                    "success": false,
                    "error": "%s",
                    "suggestion": "请重新表述查询请求或联系管理员"
                }
                """, e.getMessage());

            return Msg.builder()
                    .name("SQL查询智能体")
                    .role(MsgRole.ASSISTANT)
                    .textContent(errorResponse)
                    .metadata(Map.of(
                            "duration", duration,
                            "success", false))
                    .build();
        }
    }

}
