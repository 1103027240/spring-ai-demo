package cn.example.base.demo.service;

import cn.example.base.demo.dto.MessageDocumentVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;

/**
 * @author 11030
 */
public interface CustomerServiceWorkflowService {

    Map<String, Object> executeWorkflow(String message, Long userId, String userName);

    Page<MessageDocumentVO> pageSearch(Long userId, String currentPage, String pageSize);

}
