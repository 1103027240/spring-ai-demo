package cn.getech.base.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
public interface VectorStoreService {

    //void createCollection();

    void storeDocuments(List<Document> documents);

    List<Document> search(String query, int topK);

    Map<String, List<Document>> batchSearch(List<String> queries, int topK);

    Page<Document> pageSearch(String query, int page, int size);

    List<Document> searchByHash(String query, String hashValue);

    void deleteDocument(String docId);

    void clearCollection();

}
