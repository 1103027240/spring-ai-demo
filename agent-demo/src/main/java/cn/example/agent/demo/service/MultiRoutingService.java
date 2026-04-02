package cn.example.agent.demo.service;

import java.util.Map;

public interface MultiRoutingService {

    Map<String, Object> doChatSimple(String message);

    Map<String, Object> doChatGraph(String message);

}
