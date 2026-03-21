package cn.getech.base.demo.service;

import cn.getech.base.demo.entity.LongTermChatMemory;
import java.util.List;

/**
 * @author 11030
 */
public interface MemoryVectorService {

    String doChat(String msg, String conversationId);

    void addMemory(String msg);

    List<LongTermChatMemory> getMemory(String msg);

}
