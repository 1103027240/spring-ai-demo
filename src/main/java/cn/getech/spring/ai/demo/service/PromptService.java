package cn.getech.spring.ai.demo.service;

import cn.getech.spring.ai.demo.entity.StudentRecord;
import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
public interface PromptService {

    Flux<String> doChatRoleSystem(String msg);

    Flux<String> doChatRoleTool(String msg);

    Flux<String> doChatRoleCombine(String msg);

    StudentRecord doChatOutput(String name, String id, Integer age, String email);

}
