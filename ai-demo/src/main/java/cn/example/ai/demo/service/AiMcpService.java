package cn.example.ai.demo.service;

import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
public interface AiMcpService {

    Flux<String> doChat(String msg);

}
