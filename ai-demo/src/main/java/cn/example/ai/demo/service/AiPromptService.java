package cn.example.ai.demo.service;

import cn.example.ai.demo.entity.Student;
import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
public interface AiPromptService {

    Flux<String> doChatRoleSystem(String msg);

    Flux<String> doChatRoleTool(String msg);

    Flux<String> doChatRoleCombine(String msg);

    Student doChatOutput(String name, String id, Integer age, String email);

}
