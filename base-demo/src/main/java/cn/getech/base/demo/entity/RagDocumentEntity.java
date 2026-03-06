package cn.getech.base.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * milvus数据库中rag_document集合
 * @author 11030
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RagDocumentEntity extends MilvusDocumentEntity {

    /**
     * 文档来源
     * 描述文档的原始来源或出处
     * 示例值:
     * - "company-internal-wiki": 公司内部Wiki
     * - "technical-documentation": 技术文档
     * - "external-api-docs": 外部API文档
     * - "user-uploaded-file": 用户上传文件
     * - "web-crawled": 网页爬取
     * - "database-export": 数据库导出
     * - "email-archive": 邮件存档
     * - "chat-history": 聊天记录
     *
     * 用途:
     * 1. 用于过滤和分类搜索结果
     * 2. 跟踪知识库中不同来源文档的比例
     * 3. 评估文档的可信度和权威性
     * 4. 实现基于来源的权限控制
     */
    private String source;

    /**
     * 文档类型
     * 描述文档的格式和结构类型
     * 示例值:
     * - "text/plain": 纯文本文件
     * - "application/pdf": PDF文档
     * - "application/msword": Word文档
     * - "text/html": HTML网页
     * - "application/json": JSON数据
     * - "text/markdown": Markdown文档
     * - "image": 图像文档（OCR处理后）
     * - "presentation": 演示文稿
     * - "spreadsheet": 电子表格
     * - "code": 代码文件
     *
     * 用途:
     * 1. 针对不同文档类型采用不同的预处理策略
     * 2. 检索时考虑文档类型的相关性
     * 3. 统计知识库中各种文档类型的分布
     * 4. 优化显示格式（如PDF保留格式，代码高亮等）
     */
    private String documentType;

    /**
     * 创建时间
     * 文档在系统中被创建的时间戳
     * 格式: ISO-8601 (yyyy-MM-dd'T'HH:mm:ss)
     * 示例值: "2024-03-15T10:30:00"
     *
     * 用途:
     * 1. 时间范围过滤（如最近7天添加的文档）
     * 2. 文档新旧程度评估，优先显示较新的文档
     * 3. 审计和跟踪文档生命周期
     * 4. 清理过期或过时的文档
     * 5. 统计知识库的增长趋势
     */
    private Long createdAt;

    /**
     * 更新时间
     * 文档内容或元数据最近被修改的时间戳
     * 格式: ISO-8601 (yyyy-MM-dd'T'HH:mm:ss)
     * 示例值: "2024-03-15T14:45:30"
     *
     * 用途:
     * 1. 识别最近更新的文档，确保信息的时效性
     * 2. 缓存失效判断，仅当文档更新时重新生成向量
     * 3. 版本控制，跟踪文档的修改历史
     * 4. 同步检查，避免重复更新
     * 5. 优先展示最近更新的相关文档
     */
    private Long updatedAt;

    /**
     * 自定义属性映射
     * 存储文档的扩展属性和附加信息
     * 示例值:
     * {
     *   "author": "张三",
     *   "department": "研发部",
     *   "project": "智能助手项目",
     *   "language": "zh-CN",
     *   "version": "2.1.0",
     *   "confidence": 0.95,
     *   "readability": 8.5,
     *   "keywords": ["人工智能", "RAG", "向量搜索"],
     *   "summary": "本文档介绍了RAG系统的基本原理...",
     *   "references": ["doc_001", "doc_002"],
     *   "accessLevel": "internal",
     *   "expirationDate": "2025-12-31",
     *   "reviewStatus": "approved",
     *   "reviewer": "李四",
     *   "tags": ["技术文档", "教程", "入门指南"]
     * }
     *
     * 用途:
     * 1. 灵活扩展文档属性，无需修改表结构
     * 2. 支持复杂的业务逻辑和过滤条件
     * 3. 存储文档的语义信息和上下文
     * 4. 实现个性化的检索和排序策略
     * 5. 与其他系统集成时传递额外信息
     */
    private Map<String, Object> customAttributes;

    /**
     * 分割算法
     * 标识文档内容使用的文本分割策略
     * 示例值:
     * - "character": 字符分割（按固定字符数分割）
     * - "token": Token分割（按Token数量分割）
     * - "sentence": 句子分割（按句子边界分割）
     * - "paragraph": 段落分割（按自然段落分割）
     * - "semantic": 语义分割（基于语义理解分割）
     * - "recursive": 递归分割（多种策略组合）
     * - "fixed-size": 固定大小分割
     * - "overlap-sliding": 重叠滑动窗口分割
     *
     * 用途:
     * 1. 追溯文档的处理过程和参数
     * 2. 评估不同分割算法对检索效果的影响
     * 3. 实现分割算法的A/B测试
     * 4. 重新处理文档时使用相同的分割策略
     * 5. 调试和优化分割效果
     */
    private String splitAlgorithm;

    /**
     * 哈希值
     * 用于文档去重的唯一标识符
     * 生成方式: SHA-256(content)
     * 示例值: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
     *
     * 用途:
     * 1. 快速检测重复文档，避免数据冗余
     * 2. 增量更新，只处理新内容
     * 3. 内容完整性验证，防止数据篡改
     * 4. 建立文档间的引用关系
     * 5. 跨知识库的文档去重
     * 6. 缓存文档处理结果，提高效率
     *
     * 去重流程:
     * 1. 计算新文档内容的哈希值
     * 2. 查询哈希索引，检查是否存在相同哈希
     * 3. 如果存在，跳过存储或标记为重复
     * 4. 如果不存在，存储文档并建立哈希索引
     */
    private String hashValue;

    /**
     * 原始相似度分数
     * 文档在向量检索中与查询的原始相似度得分
     * 范围: 0.0 ~ 1.0（1.0表示完全匹配）
     * 示例值: 0.85
     *
     * 用途:
     * 1. 初步相关性排序，作为基础排序依据
     * 2. 阈值过滤，排除低相关性文档（如<0.3）
     * 3. 检索质量评估，监控系统效果
     * 4. 重排算法的输入，结合其他特征重新排序
     * 5. 解释搜索结果，向用户展示匹配程度
     * 6. 训练数据收集，优化模型和参数
     *
     * 计算方式:
     * 1. 余弦相似度: cosθ = A·B / (||A||·||B||)
     * 2. 内积相似度: A·B
     * 3. 欧氏距离: 1 / (1 + distance)
     * 4. 其他自定义相似度函数
     */
    private Double originalScore;


}
