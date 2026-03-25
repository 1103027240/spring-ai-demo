package cn.example.base.demo.config.loop;

import cn.hutool.core.collection.CollUtil;
import org.springframework.ai.chat.messages.Message;
import java.util.List;
import java.util.function.Predicate;

public class SimpleCustomerServiceLoopCondition implements Predicate<List<Message>> {

    @Override
    public boolean test(List<Message> messages) {
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

        // 返回数据中结束标志
        if(lastContent.contains("\"needMoreInfo\": false")){
            return true;
        }

        return false;
    }

}
