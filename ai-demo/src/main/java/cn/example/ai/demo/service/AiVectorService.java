package cn.example.ai.demo.service;

import cn.example.ai.demo.entity.LongTermChatMemory;
import java.util.List;

/**
 * @author 11030
 */
public interface AiVectorService {

    void addVector(String msg);

    List<LongTermChatMemory> getVector(String msg);

}
