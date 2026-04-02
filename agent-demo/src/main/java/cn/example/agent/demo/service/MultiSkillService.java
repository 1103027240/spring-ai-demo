package cn.example.agent.demo.service;

import java.util.Map;

public interface MultiSkillService {

    Map<String, Object> doChatSqlAssistant(String message);

    Map<String, Object> doChatInventoryManagement(String message);

    Map<String, Object> doChatSalesAnalysis(String message);

}
