package cn.example.base.demo.config.loop;

import cn.hutool.core.collection.CollUtil;
import org.springframework.ai.chat.messages.Message;
import java.util.List;
import java.util.function.Predicate;

public class SimpleCustomerServiceLoopCondition implements Predicate<List<Message>> {

    private Integer maxLoopTimes;

    private Integer count = 0;

    public SimpleCustomerServiceLoopCondition(int maxLoopTimes) {
        this.maxLoopTimes = maxLoopTimes;
    }

    @Override
    public boolean test(List<Message> messages) {
        count++;
        if (CollUtil.isEmpty(messages)) {
            return false;
        }

        // 1、校验是否达到最大循环次数
        if(count > maxLoopTimes){
            return true;
        }

        // 获取最后一条消息
        Message lastMessage = messages.get(messages.size() - 1);
        String lastContent = lastMessage.getText();

        // 2、校验返回数据中是否有结束关键词
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

        // 3、校验返回数据中是否结束标志
        if(lastContent.contains("\"needMoreInfo\": false")){
            return true;
        }

        return false;
    }

}
