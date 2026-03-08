package cn.getech.base.demo.node.email;

import cn.getech.base.demo.dto.EmailClassifyDto;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

/**
 * 搜索文档节点
 * @author 11030
 */
@Slf4j
public class SearchDocumentNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 构建搜索查询
        EmailClassifyDto emailClassifyDto = state.value("emailClassify")
                .map(v -> (EmailClassifyDto) v)
                .orElse(new EmailClassifyDto());

        String query = emailClassifyDto.getIntent() + " " + emailClassifyDto.getTopic();

        try {
            // 返回结果（生产调用AI大模型）
            List<String> searchResults = List.of(
                    "通过设置 > 安全 > 更改密码重置密码",
                    "密码必须至少12个字符",
                    "包含大写字母、小写字母、数字和符号");

            return Map.of("search_results", searchResults, "next_node", "draft_response");
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("search error: {}", e.getMessage());

            List<String> errorResult = List.of("搜索暂时不可用: " + e.getMessage());
            return Map.of("search_results", errorResult, "next_node", "draft_response");
        }
    }

}
