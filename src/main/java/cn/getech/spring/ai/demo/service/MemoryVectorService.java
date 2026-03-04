package cn.getech.spring.ai.demo.service;

import cn.getech.spring.ai.demo.entity.LongTermChatMemoryEntity;
import java.util.List;

/**
 * @author 11030
 */
public interface MemoryVectorService {

    String doChatHierarchical(String msg, String conversationId);

    void addMemory(String msg);

    List<LongTermChatMemoryEntity> getMemory(String msg);

}
