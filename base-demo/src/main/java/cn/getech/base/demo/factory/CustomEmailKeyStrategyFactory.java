package cn.getech.base.demo.factory;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import java.util.HashMap;

/**
 * @author 11030
 */
public class CustomEmailKeyStrategyFactory {

    /**
     * 配置策略键
     */
    public static KeyStrategyFactory emailKeyStrategyFactory() {
        return () -> {
            HashMap<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("email_content", new ReplaceStrategy());
            strategies.put("sender_email", new ReplaceStrategy());
            strategies.put("email_id", new ReplaceStrategy());
            strategies.put("emailClassify", new ReplaceStrategy());
            strategies.put("search_results", new ReplaceStrategy());
            strategies.put("customer_history", new ReplaceStrategy());
            strategies.put("draft_response", new ReplaceStrategy());
            strategies.put("messages", new AppendStrategy());
            strategies.put("next_node", new ReplaceStrategy());
            strategies.put("status", new ReplaceStrategy());
            strategies.put("review_data", new ReplaceStrategy());
            return strategies;
        };
    }

}
