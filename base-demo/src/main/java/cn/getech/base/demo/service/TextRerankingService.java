package cn.getech.base.demo.service;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 文本重排服务接口
 * @author 11030
 */
public interface TextRerankingService {

    List<Document> rerankByScore(List<Document> documents);

    List<Document> enhancedRerank(List<Document> documents, String query);

}
