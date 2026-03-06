package cn.getech.base.demo.service;

import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
public interface DemoService {

    String doChatModelQwen(String msg);

    Flux<String> doChatModelDeepseek(String msg);

    String doChatClientQwen(String msg);

    Flux<String> doChatClientDeepseek(String msg);

}
