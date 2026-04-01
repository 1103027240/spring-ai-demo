package cn.example.ai.demo.service;

import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
public interface AiDemoService {

    String doChatModelQwen(String msg);

    Flux<String> doChatModelDeepseek(String msg);

    String doChatClientQwen(String msg);

    Flux<String> doChatClientDeepseek(String msg);

}
