package cn.example.agent.demo.config.loop;

import cn.hutool.core.collection.CollUtil;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Predicate;
import static cn.example.agent.demo.constant.FieldValueConstant.ONE;

@Component
public class SimpleCustomerChatLoopCondition implements Predicate<List<Message>> {

    private int count = 0;

    @Override
    public boolean test(List<Message> messages) {
        // 检查最大迭代次数
        count++;
        if (count > ONE) {
            return true;  // 超过最大次数，返回true结束循环
        }

        if (CollUtil.isEmpty(messages)) {
            return false;
        }

        // 获取最后一条消息
        Message lastMessage = messages.get(messages.size() - 1);
        String lastContent = lastMessage.getText();

        // 结束关键词
        String[] endKeywords = {
                "谢谢", "再见", "结束", "好了", "完成",
                "不客气", "不用谢", "问题解决", "处理完成",
                "已解决", "已完成", "结束对话", "对话结束"
        };
        for (String keyword : endKeywords) {
            if (lastContent.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

}
