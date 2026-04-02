package cn.example.agent.demo.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import java.util.List;

/**
 * 消息数量达到限制，保存最近N条消息
 */
@Slf4j
@HookPositions(value = {HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL})
public class TrimmingModelHook extends MessagesModelHook {

    private static final Integer MAX_MESSAGES = 10;

    @Override
    public String getName() {
        return "TrimmingModelHook";
    }

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        log.info("【TrimmingModelHook beforeModel】开始执行");
        if(previousMessages.size() > MAX_MESSAGES){
            List<Message> messages = previousMessages.subList(previousMessages.size() - MAX_MESSAGES, previousMessages.size());
            return new AgentCommand(messages, UpdatePolicy.REPLACE);
        }
        return new AgentCommand(previousMessages);
    }

}
