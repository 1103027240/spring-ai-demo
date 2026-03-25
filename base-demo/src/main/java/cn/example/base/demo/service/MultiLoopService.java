package cn.example.base.demo.service;

import java.util.Map;

public interface MultiLoopService {

    Map<String, Object> doChat(String message, Long userId, String sessionId);

}
