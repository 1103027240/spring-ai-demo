package cn.getech.base.demo.service;

import java.util.Map;

/**
 * @author 11030
 */
public interface WorkflowExecutionService {

    Map<String, Object> executeWorkflow(String userInput, Long userId, String userName);

}
