# RAG 检索增强

## 概述

RAG（Retrieval-Augmented Generation）检索增强生成是一种结合检索和生成的技术，通过从知识库检索相关信息来增强 LLM 的回答质量。

## 技术架构

```
用户问题
    ↓
┌─────────────────┐
│   Embedding     │  ← 文本向量化
└────────┬────────┘
         ↓
┌─────────────────┐
│    Milvus       │  ← 向量检索
│   向量数据库     │
└────────┬────────┘
         ↓
┌─────────────────┐
│  相关文档片段    │
└────────┬────────┘
         ↓
┌─────────────────┐
│   LLM 生成      │  ← 结合上下文生成回答
└─────────────────┘
         ↓
      最终回答
```

## 核心组件

### 向量数据库配置

位于 `ai-demo/config/` 目录：

- `MilvusCollectionCreatorConfig.java` - Milvus 集合创建配置

### 向量存储

使用 Milvus 作为向量数据库：

| 参数 | 说明 |
|------|------|
| 集合名称 | 自定义集合名 |
| 向量维度 | 1536（OpenAI）/ 1024（阿里云） |
| 相似度度量 | L2 / IP / COSINE |

## 知识库管理

### 文档导入

支持多种文档格式：
- PDF 文档
- Word 文档
- Markdown 文档
- 纯文本

### 文档切片

将长文档切分为小块进行向量化存储：

```java
// 文档切片配置
ChunkConfig config = ChunkConfig.builder()
    .chunkSize(500)      // 每块大小
    .overlap(50)         // 重叠大小
    .build();
```

## 使用示例

```java
@Autowired
private VectorStore vectorStore;

// 存储文档
public void storeDocument(String content) {
    Document doc = new Document(content);
    vectorStore.add(List.of(doc));
}

// 相似度检索
public List<Document> search(String query) {
    return vectorStore.similaritySearch(query);
}
```

## 相关服务

| 服务类 | 功能 |
|--------|------|
| `KnowledgeService` | 知识库管理 |
| `EmbeddingService` | 文本向量化 |
| `RetrievalService` | 检索服务 |
