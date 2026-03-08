package cn.getech.base.demo.node.email;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 读取邮件节点
 * @author 11030
 */
public class ReadEmailNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 读取邮件内容（生产环境连接邮件服务）
        String emailContent = state.value("email_content", "");

        List<String> messages = new ArrayList<>();
        messages.add(emailContent);

        return Map.of("messages", messages);
    }

}
