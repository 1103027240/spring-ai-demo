-- 1. 为chat_message表添加复合索引，优化查询性能
-- =============================================

-- 优化：用户查询最近消息（按用户ID和时间查询）
CREATE INDEX IF NOT EXISTS idx_user_time ON chat_message(user_id, create_time DESC);

-- 优化：会话消息同步状态查询（复合索引）
CREATE INDEX IF NOT EXISTS idx_session_sync ON chat_message(session_id, sync_status, is_deleted);

-- 优化：按工作流执行ID查询消息
CREATE INDEX IF NOT EXISTS idx_workflow_exec_deleted ON chat_message(workflow_execution_id, is_deleted);

-- 优化：AI响应消息查询
CREATE INDEX IF NOT EXISTS idx_ai_response_time ON chat_message(is_ai_response, response_time, create_time DESC);

-- 优化：意图和情感分析查询
CREATE INDEX IF NOT EXISTS idx_intent_sentiment ON chat_message(intent, sentiment, create_time DESC);


-- 2. 为chat_session表添加复合索引
-- =============================================

-- 优化：用户活跃会话查询
CREATE INDEX IF NOT EXISTS idx_user_status_time ON chat_session(user_id, status, last_message_time DESC);

-- 优化：按时间和类型查询会话
CREATE INDEX IF NOT EXISTS idx_type_start_time ON chat_session(session_type, start_time DESC);


-- 3. 为workflow_execution表添加复合索引
-- =============================================

-- 优化：用户工作流执行记录查询
CREATE INDEX IF NOT EXISTS idx_user_start_time ON workflow_execution(user_id, start_time DESC);

-- 优化：工作流执行状态查询
CREATE INDEX IF NOT EXISTS idx_status_time ON workflow_execution(status, start_time DESC);


-- 4. 为message_sync_task表添加复合索引
-- =============================================

-- 优化：待处理的同步任务查询
CREATE INDEX IF NOT EXISTS idx_session_status_time ON message_sync_task(session_id, status, created_time DESC);

-- 优化：失败的重试任务查询
CREATE INDEX IF NOT EXISTS idx_status_retry_time ON message_sync_task(status, retry_count, updated_time DESC);


-- 5. 表分区优化（可选，适用于大数据量场景）
-- =============================================

-- 对chat_message表按时间范围分区（按月分区）
/*
ALTER TABLE chat_message
PARTITION BY RANGE (UNIX_TIMESTAMP(create_time)/86400 DIV 30) (
    PARTITION p202401 VALUES LESS THAN (UNIX_TIMESTAMP('2024-02-01')/86400 DIV 30),
    PARTITION p202402 VALUES LESS THAN (UNIX_TIMESTAMP('2024-03-01')/86400 DIV 30),
    PARTITION p202403 VALUES LESS THAN (UNIX_TIMESTAMP('2024-04-01')/86400 DIV 30),
    PARTITION p202404 VALUES LESS THAN (UNIX_TIMESTAMP('2024-05-01')/86400 DIV 30),
    PARTITION p202405 VALUES LESS THAN (UNIX_TIMESTAMP('2024-06-01')/86400 DIV 30),
    PARTITION p202406 VALUES LESS THAN (UNIX_TIMESTAMP('2024-07-01')/86400 DIV 30),
    PARTITION p202407 VALUES LESS THAN (UNIX_TIMESTAMP('2024-08-01')/86400 DIV 30),
    PARTITION p202408 VALUES LESS THAN (UNIX_TIMESTAMP('2024-09-01')/86400 DIV 30),
    PARTITION p202409 VALUES LESS THAN (UNIX_TIMESTAMP('2024-10-01')/86400 DIV 30),
    PARTITION p202410 VALUES LESS THAN (UNIX_TIMESTAMP('2024-11-01')/86400 DIV 30),
    PARTITION p202411 VALUES LESS THAN (UNIX_TIMESTAMP('2024-12-01')/86400 DIV 30),
    PARTITION p202412 VALUES LESS THAN (UNIX_TIMESTAMP('2025-01-01')/86400 DIV 30),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
*/


-- 6. 优化MySQL参数配置
-- =============================================

-- 查看当前参数配置
-- SHOW VARIABLES LIKE 'innodb_buffer_pool_size';
-- SHOW VARIABLES LIKE 'innodb_log_file_size';
-- SHOW VARIABLES LIKE 'innodb_flush_log_at_trx_commit';
-- SHOW VARIABLES LIKE 'query_cache_size';

-- 建议的MySQL参数优化（需要在my.cnf或my.ini中配置）
/*
[mysqld]
# InnoDB缓冲池大小（建议设置为物理内存的70-80%）
innodb_buffer_pool_size = 8G

# InnoDB日志文件大小（建议256M-1G）
innodb_log_file_size = 512M

# 提交策略：0-每秒提交，1-每次提交，2-每秒提交但写入日志
innodb_flush_log_at_trx_commit = 2

# InnoDB日志缓冲区大小
innodb_log_buffer_size = 64M

# 并发线程数
innodb_thread_concurrency = 16

# 临时表大小
tmp_table_size = 512M
max_heap_table_size = 512M

# 查询缓存（MySQL 8.0已移除，5.7可以使用）
query_cache_size = 256M
query_cache_type = 1

# 连接数
max_connections = 500

# 慢查询日志
slow_query_log = 1
long_query_time = 1
slow_query_log_file = /var/log/mysql/slow-query.log
*/


-- 7. 查看索引使用情况
-- =============================================

-- 查看chat_message表的索引使用情况
-- SELECT 
--     TABLE_NAME,
--     INDEX_NAME,
--     SEQ_IN_INDEX,
--     COLUMN_NAME,
--     CARDINALITY
-- FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = 'ai_demo'
--   AND TABLE_NAME = 'chat_message'
-- ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 查看表的统计信息
-- SHOW TABLE STATUS LIKE 'chat_message';

-- 查看索引碎片化情况
-- SELECT 
--     TABLE_NAME,
--     ENGINE,
--     ROUND(data_length/1024/1024, 2) AS data_mb,
--     ROUND(index_length/1024/1024, 2) AS index_mb,
--     ROUND(data_free/1024/1024, 2) AS free_mb,
--     ROUND((data_free/(data_length+index_length))*100, 2) AS fragmentation_percent
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = 'ai_demo'
--   AND TABLE_NAME IN ('chat_message', 'chat_session', 'workflow_execution');


-- 8. 定期维护建议
-- =============================================

-- 定期优化表（建议在低峰期执行）
-- OPTIMIZE TABLE chat_message;
-- OPTIMIZE TABLE chat_session;
-- OPTIMIZE TABLE workflow_execution;

-- 分析表以更新统计信息
-- ANALYZE TABLE chat_message;
-- ANALYZE TABLE chat_session;
-- ANALYZE TABLE workflow_execution;

-- 检查表完整性
-- CHECK TABLE chat_message;


-- 9. 监控慢查询
-- =============================================

-- 查看慢查询日志
-- SHOW VARIABLES LIKE 'slow_query_log%';
-- SHOW VARIABLES LIKE 'long_query_time';

-- 查看最近的慢查询
-- SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;


-- 10. 索引优化验证
-- =============================================

-- 验证索引是否被使用
-- EXPLAIN SELECT * FROM chat_message WHERE user_id = 123 AND is_deleted = 0 ORDER BY create_time DESC LIMIT 20;

-- 验证复合索引是否被使用
-- EXPLAIN SELECT * FROM chat_message WHERE session_id = 'xxx' AND sync_status = 0 AND is_deleted = 0 ORDER BY id ASC;

-- 验证清理旧消息的SQL是否使用了索引
-- EXPLAIN SELECT id FROM chat_message WHERE user_id = 123 AND is_deleted = 0 ORDER BY id ASC LIMIT 10;


-- =============================================
-- 执行说明
-- =============================================
-- 1. 在低峰期执行此脚本
-- 2. 先备份数据库
-- 3. 分段执行，观察性能影响
-- 4. 使用EXPLAIN验证索引效果
-- 5. 定期监控慢查询日志
-- =============================================
