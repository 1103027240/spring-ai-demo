package cn.example.base.demo.service;

import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
public interface McpService {

    Flux<String> doChat(String msg);

}
