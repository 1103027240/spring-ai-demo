package cn.example.base.demo.service;

import cn.example.base.demo.entity.Student;
import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
public interface PromptService {

    Flux<String> doChatRoleSystem(String msg);

    Flux<String> doChatRoleTool(String msg);

    Flux<String> doChatRoleCombine(String msg);

    Student doChatOutput(String name, String id, Integer age, String email);

}
