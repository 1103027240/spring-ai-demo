package cn.example.ai.demo.service;

import java.util.List;

/**
 * query查询改写服务接口
 * @author 11030
 */
public interface QueryRewriteService {

    List<String> rewriteQuery(String originalQuery);

}
