DROP TABLE IF EXISTS `workflow_execution`;
DROP TABLE IF EXISTS `chat_session`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `message_sync_task`;
DROP TABLE IF EXISTS `knowledge_document`;
DROP TABLE IF EXISTS `knowledge_category`;
DROP TABLE IF EXISTS `chat_intent_stat`;
DROP TABLE IF EXISTS `chat_sentiment_stat`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `after_sales`;
DROP TABLE IF EXISTS `system_config`;

-- 4.1 工作流执行记录表
CREATE TABLE `workflow_execution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `execution_id` varchar(100) NOT NULL COMMENT '执行唯一标识',
  `workflow_name` varchar(100) NOT NULL COMMENT '工作流名称',
  `session_id` varchar(100) DEFAULT NULL COMMENT '会话ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `input_data` json DEFAULT NULL COMMENT '输入数据（JSON格式）',
  `output_data` json DEFAULT NULL COMMENT '输出数据（JSON格式）',
  `status` varchar(20) DEFAULT 'RUNNING' COMMENT '状态(RUNNING,SUCCESS,FAILED,TIMEOUT,CANCELLED)',
  `error_message` text COMMENT '错误信息',
  `node_results` json DEFAULT NULL COMMENT '节点执行结果（JSON格式）',
  `execution_path` text COMMENT '执行路径',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `max_retries` int(11) DEFAULT '3' COMMENT '最大重试次数',
  `timeout_seconds` int(11) DEFAULT '30' COMMENT '超时时间（秒）',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration_ms` bigint(20) DEFAULT NULL COMMENT '执行时长（毫秒）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_execution_id` (`execution_id`) USING BTREE,
  KEY `idx_session_id` (`session_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_workflow_name` (`workflow_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流执行记录表';

-- 4.2 客服会话表
CREATE TABLE `chat_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(100) NOT NULL COMMENT '会话唯一标识',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `session_type` tinyint(4) DEFAULT '1' COMMENT '会话类型(1:咨询,2:售后,3:投诉,4:建议,5:其他)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(1:进行中,0:已结束,2:已转人工,3:等待回复)',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `message_count` int(11) DEFAULT '0' COMMENT '消息数量',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `session_data` json DEFAULT NULL COMMENT '会话数据（JSON格式）',
  `avg_response_time` int(11) DEFAULT NULL COMMENT '平均响应时间（毫秒）',
  `satisfaction_score` tinyint(4) DEFAULT NULL COMMENT '满意度评分（1-5）',
  `tags` json DEFAULT NULL COMMENT '标签（JSON数组）',
  `channel` varchar(20) DEFAULT 'web' COMMENT '渠道(web,app,wechat,other)',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_session_id` (`session_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_last_message_time` (`last_message_time`) USING BTREE,
  KEY `idx_session_type` (`session_type`) USING BTREE,
  KEY `idx_channel` (`channel`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服会话表';

-- 4.3 聊天消息表
CREATE TABLE `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` varchar(100) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `message_type` tinyint(4) NOT NULL COMMENT '消息类型(1:用户,2:AI回复,3:系统,4:客服)',
  `content` text NOT NULL COMMENT '消息内容',
  `intent` varchar(50) DEFAULT NULL COMMENT '意图',
  `sentiment` varchar(20) DEFAULT NULL COMMENT '情感(positive,neutral,negative,urgent)',
  `is_ai_response` tinyint(4) DEFAULT '0' COMMENT '是否为AI回复(0:否,1:是)',
  `workflow_execution_id` varchar(100) DEFAULT NULL COMMENT '工作流执行ID',
  `sync_status` tinyint(4) DEFAULT '0' COMMENT '同步状态(0:未同步,1:同步中,2:已同步,3:同步失败,4:跳过同步)',
  `sync_time` bigint(64) COMMENT '同步时间',
  `response_time` int(11) DEFAULT NULL COMMENT '响应时间（毫秒）',
  `is_deleted` tinyint(4) DEFAULT '0' COMMENT '是否已删除(0:否,1:是)',
  `metadata` json DEFAULT NULL COMMENT '元数据（JSON格式）',
  `vector_id` varchar(100) DEFAULT NULL COMMENT '向量ID（Milvus）',
  `embedding_status` tinyint(4) DEFAULT '0' COMMENT '向量化状态(0:未向量化,1:已向量化,2:向量化失败)',
  `embedding_time` bigint(64) COMMENT '向量化时间',
  `similarity_score` double DEFAULT NULL COMMENT '相似度分数（用于检索）',
  `create_time` bigint(64) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_session_id` (`session_id`) USING BTREE,
  KEY `idx_sync_status` (`sync_status`,`is_deleted`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_workflow_exec` (`workflow_execution_id`) USING BTREE,
  KEY `idx_message_type` (`message_type`) USING BTREE,
  KEY `idx_is_ai_response` (`is_ai_response`) USING BTREE,
  KEY `idx_embedding_status` (`embedding_status`) USING BTREE,
  KEY `idx_intent` (`intent`) USING BTREE,
  KEY `idx_sentiment` (`sentiment`) USING BTREE,
  FULLTEXT KEY `ft_content` (`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 4.4 消息同步任务表
CREATE TABLE `message_sync_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `session_id` varchar(100) NOT NULL COMMENT '会话ID',
  `sync_type` tinyint(4) DEFAULT '1' COMMENT '同步类型(1:增量,2:全量,3:强制)',
  `last_message_id` bigint(20) DEFAULT NULL COMMENT '最后消息ID',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态(0:待处理,1:处理中,2:已完成,3:已失败,4:已取消)',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `max_retries` int(11) DEFAULT '3' COMMENT '最大重试次数',
  `error_message` text COMMENT '错误信息',
  `progress` int(11) DEFAULT '0' COMMENT '进度（0-100）',
  `total_messages` int(11) DEFAULT '0' COMMENT '总消息数',
  `processed_messages` int(11) DEFAULT '0' COMMENT '已处理消息数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_session_pending` (`session_id`,`status`) USING BTREE,
  KEY `idx_status_retry` (`status`,`retry_count`) USING BTREE,
  KEY `idx_created_time` (`created_time`) USING BTREE,
  KEY `idx_updated_time` (`updated_time`) USING BTREE,
  KEY `idx_sync_type` (`sync_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息同步任务表';

-- 4.5 知识库文档表
CREATE TABLE `knowledge_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `summary` text COMMENT '摘要',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `tags` json DEFAULT NULL COMMENT '标签（JSON数组）',
  `keywords` json DEFAULT NULL COMMENT '关键词（JSON数组）',
  `source` varchar(100) DEFAULT NULL COMMENT '来源',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `priority` int(11) DEFAULT '0' COMMENT '优先级',
  `is_vectorized` tinyint(4) DEFAULT '0' COMMENT '是否已向量化(0:否,1:是)',
  `vector_id` varchar(100) DEFAULT NULL COMMENT '向量ID',
  `version` int(11) DEFAULT '1' COMMENT '版本号',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(1:启用,0:禁用,2:待审核,3:已删除)',
  `view_count` int(11) DEFAULT '0' COMMENT '查看次数',
  `useful_count` int(11) DEFAULT '0' COMMENT '有用次数',
  `useless_count` int(11) DEFAULT '0' COMMENT '无用次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_category` (`category`) USING BTREE,
  KEY `idx_is_vectorized` (`is_vectorized`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE,
  KEY `idx_author` (`author`) USING BTREE,
  KEY `idx_view_count` (`view_count`) USING BTREE,
  FULLTEXT KEY `ft_title_content` (`title`,`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- 4.6 知识分类表
CREATE TABLE `knowledge_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父分类ID',
  `level` int(11) DEFAULT '1' COMMENT '层级',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `color` varchar(20) DEFAULT NULL COMMENT '颜色',
  `document_count` int(11) DEFAULT '0' COMMENT '文档数量',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(1:启用,0:禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_category_name` (`category_name`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_level` (`level`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识分类表';

-- 4.7 用户表
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像',
  `gender` tinyint(4) DEFAULT '0' COMMENT '性别(0:未知,1:男,2:女)',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `level` tinyint(4) DEFAULT '1' COMMENT '用户等级(1:普通,2:银卡,3:金卡,4:白金,5:钻石)',
  `points` int(11) DEFAULT '0' COMMENT '积分',
  `registration_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `login_count` int(11) DEFAULT '0' COMMENT '登录次数',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(1:正常,0:禁用,2:未激活,3:已注销)',
  `user_data` json DEFAULT NULL COMMENT '用户数据（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  KEY `idx_phone` (`phone`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `idx_level` (`level`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_registration_time` (`registration_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 4.8 订单表
CREATE TABLE `order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_number` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '折扣金额',
  `actual_amount` decimal(10,2) DEFAULT '0.00' COMMENT '实付金额',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态(0:待支付,1:已支付,2:已发货,3:已完成,4:已取消,5:退款中,6:已退款,7:部分退款)',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `shipping_address` varchar(500) DEFAULT NULL COMMENT '收货地址',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `logistics_company` varchar(50) DEFAULT NULL COMMENT '物流公司',
  `tracking_number` varchar(100) DEFAULT NULL COMMENT '运单号',
  `shipping_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `order_items` json DEFAULT NULL COMMENT '订单商品（JSON数组）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_number` (`order_number`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_payment_time` (`payment_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 4.9 售后表
CREATE TABLE `after_sales` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  `service_number` varchar(50) NOT NULL COMMENT '服务单号',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `order_item_id` bigint(20) DEFAULT NULL COMMENT '订单商品ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` tinyint(4) NOT NULL COMMENT '售后类型(1:退货,2:换货,3:维修,4:补发,5:退款)',
  `reason` varchar(500) DEFAULT NULL COMMENT '售后原因',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态(0:待处理,1:处理中,2:已完成,3:已关闭,4:已取消)',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `solution` varchar(500) DEFAULT NULL COMMENT '解决方案',
  `processor_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `attachments` json DEFAULT NULL COMMENT '附件（JSON数组）',
  `customer_feedback` varchar(500) DEFAULT NULL COMMENT '客户反馈',
  `satisfaction_score` tinyint(4) DEFAULT NULL COMMENT '满意度评分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_service_number` (`service_number`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_processor_id` (`processor_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后表';

-- 4.10 意图统计表
CREATE TABLE `chat_intent_stat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `intent` varchar(50) NOT NULL COMMENT '意图',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `count` int(11) DEFAULT '0' COMMENT '次数',
  `success_count` int(11) DEFAULT '0' COMMENT '成功次数',
  `fail_count` int(11) DEFAULT '0' COMMENT '失败次数',
  `avg_response_time` int(11) DEFAULT NULL COMMENT '平均响应时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_intent_date` (`intent`,`stat_date`) USING BTREE,
  KEY `idx_stat_date` (`stat_date`) USING BTREE,
  KEY `idx_intent` (`intent`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意图统计表';

-- 4.11 情感统计表
CREATE TABLE `chat_sentiment_stat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `sentiment` varchar(20) NOT NULL COMMENT '情感',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `count` int(11) DEFAULT '0' COMMENT '次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sentiment_date` (`sentiment`,`stat_date`) USING BTREE,
  KEY `idx_stat_date` (`stat_date`) USING BTREE,
  KEY `idx_sentiment` (`sentiment`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感统计表';

-- 4.12 系统配置表
CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_desc` varchar(500) COMMENT '配置描述',
  `config_type` tinyint(4) DEFAULT '1' COMMENT '配置类型(1:系统,2:业务,3:界面,4:安全)',
  `data_type` varchar(20) DEFAULT 'string' COMMENT '数据类型(string,number,boolean,json)',
  `options` json DEFAULT NULL COMMENT '选项（JSON格式）',
  `validation` varchar(200) DEFAULT NULL COMMENT '验证规则',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(1:启用,0:禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_config_key` (`config_key`) USING BTREE,
  KEY `idx_config_type` (`config_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================
-- 5. 初始化表数据
-- ============================================

-- 5.1 初始化系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`, `config_type`, `data_type`) VALUES
-- 系统配置
('system.mode', 'development', '系统运行模式(development,test,production)', 1, 'string'),
('system.version', '2.0.0', '系统版本', 1, 'string'),
('system.maintenance', 'false', '是否处于维护模式', 1, 'boolean'),
('system.name', 'AI智能客服系统', '系统名称', 1, 'string'),
('system.copyright', '© 2025 AI Customer Service', '版权信息', 1, 'string'),

-- 聊天配置
('chat.message.retention_count', '20', 'MySQL中保存的聊天消息数量，超过此数量的消息会被标记为删除', 2, 'number'),
('chat.session.timeout_minutes', '30', '会话超时时间（分钟）', 2, 'number'),
('chat.intent.enabled', 'true', '是否启用意图识别', 2, 'boolean'),
('chat.sentiment.enabled', 'true', '是否启用情感分析', 2, 'boolean'),
('chat.auto_response', 'true', '是否启用自动回复', 2, 'boolean'),
('chat.max_history_messages', '20', '最大历史消息数量', 2, 'number'),

-- 同步配置
('sync.max_retry_count', '3', '同步任务最大重试次数', 2, 'number'),
('sync.retry_delay_seconds', '5', '同步失败后的重试延迟时间（秒）', 2, 'number'),
('sync.batch_size', '100', '批量同步大小', 2, 'number'),
('sync.interval_seconds', '60', '同步间隔时间（秒）', 2, 'number'),

-- 工作流配置
('workflow.timeout_seconds', '30', '工作流执行超时时间（秒）', 2, 'number'),
('workflow.max_retries', '3', '工作流最大重试次数', 2, 'number'),
('workflow.recursion_limit', '25', '工作流递归限制', 2, 'number'),
('workflow.default_workflow', 'customer_service', '默认工作流', 2, 'string'),

-- 向量检索配置
('vector.similarity_threshold', '0.6', '向量相似度阈值，用于检索相关文档', 2, 'number'),
('vector.top_k', '5', '向量检索返回的最相似文档数量', 2, 'number'),
('vector.collection_name', 'customer_knowledge', 'Milvus集合名称', 2, 'string'),
('vector.embedding_model', 'text-embedding-ada-002', '嵌入模型名称', 2, 'string'),

-- AI模型配置
('ai.model.name', 'qwen-max', '使用的AI模型名称', 2, 'string'),
('ai.model.provider', 'dashscope', 'AI模型提供商', 2, 'string'),
('ai.temperature', '0.7', 'AI生成温度参数', 2, 'number'),
('ai.max_tokens', '2000', 'AI生成最大token数量', 2, 'number'),
('ai.api_key', '', 'AI API密钥', 4, 'string'),

-- 知识库配置
('knowledge.vector.enabled', 'true', '是否启用知识库向量化', 2, 'boolean'),
('knowledge.search.limit', '10', '知识库搜索返回的最大结果数', 2, 'number'),
('knowledge.auto_category', 'true', '是否自动分类', 2, 'boolean'),
('knowledge.review_required', 'false', '知识库内容是否需要审核', 2, 'boolean'),

-- 缓存配置
('redis.cache.enabled', 'true', '是否启用Redis缓存', 2, 'boolean'),
('redis.cache.ttl.workflow', '3600', '工作流状态缓存时间（秒）', 2, 'number'),
('redis.cache.ttl.session', '1800', '会话缓存时间（秒）', 2, 'number'),
('redis.cache.ttl.knowledge', '7200', '知识库缓存时间（秒）', 2, 'number'),
('redis.cache.ttl.user', '3600', '用户信息缓存时间（秒）', 2, 'number'),

-- 邮件配置
('email.smtp.host', 'smtp.example.com', 'SMTP服务器地址', 3, 'string'),
('email.smtp.port', '587', 'SMTP服务器端口', 3, 'number'),
('email.smtp.username', 'noreply@example.com', 'SMTP用户名', 3, 'string'),
('email.smtp.password', '', 'SMTP密码', 3, 'string'),
('email.smtp.ssl', 'true', '是否启用SSL', 3, 'boolean'),
('email.from.address', 'noreply@example.com', '发件人邮箱', 3, 'string'),
('email.from.name', 'AI客服系统', '发件人名称', 3, 'string'),

-- 安全配置
('security.jwt.secret', 'your_jwt_secret_key_here', 'JWT密钥', 4, 'string'),
('security.jwt.expiration', '86400', 'JWT过期时间（秒）', 4, 'number'),
('security.password.min_length', '8', '密码最小长度', 4, 'number'),
('security.login.max_attempts', '5', '最大登录尝试次数', 4, 'number'),
('security.login.lockout_minutes', '30', '账户锁定时间（分钟）', 4, 'number'),
('security.ip.whitelist', '[]', 'IP白名单', 4, 'json');

-- 5.2 初始化知识分类
INSERT INTO `knowledge_category` (`category_name`, `parent_id`, `level`, `sort_order`, `description`) VALUES
('售后政策', 0, 1, 1, '退货、换货、退款等相关政策'),
('物流配送', 0, 1, 2, '发货、物流、配送等相关信息'),
('商品咨询', 0, 1, 3, '商品规格、价格、库存等信息'),
('支付问题', 0, 1, 4, '支付、退款、发票等问题'),
('会员权益', 0, 1, 5, '会员等级、权益、积分等信息'),
('常见问题', 0, 1, 6, '常见问题解答'),
('技术支持', 0, 1, 7, '技术问题、使用教程等'),
('关于我们', 0, 1, 8, '公司介绍、联系我们等');

-- 5.3 初始化知识库文档
INSERT INTO `knowledge_document` (`title`, `content`, `category`, `tags`, `summary`, `priority`, `author`) VALUES
-- 售后政策相关
('七天无理由退货政策', 
'# 七天无理由退货政策

## 一、适用范围
1. 自商品签收之日起7天内，商品完好、未使用、包装附件齐全，可申请七天无理由退货。
2. 以下商品不适用：定制类商品、鲜活易腐类、在线下载或已拆封的音像制品、计算机软件、报纸期刊等。

## 二、退货流程
1. 登录账号，进入"我的订单"选择要退货的商品
2. 填写退货原因，上传商品照片
3. 系统审核通过后生成退货取件码
4. 快递员上门取件或自行寄回
5. 商家收货验货，确认无误后1-3个工作日内退款

## 三、退款说明
1. 退款将原路返回支付账户
2. 使用优惠券支付的订单，优惠券不退
3. 退货产生的运费由买家承担，购买运费险的可申请理赔

## 四、注意事项
1. 商品需保持原包装完好，配件齐全
2. 赠品需一并退回
3. 如有赠品遗失，按实际价值扣除相应费用',
'售后政策', 
'["退货", "退款", "七天无理由", "政策"]',
'7天无理由退货政策详细说明，包含适用范围、流程、退款说明和注意事项。',
100, '系统管理员'),

('商品质量与保修', 
'# 商品质量与保修

## 一、保修期限
1. 手机/电脑：整机保修1年，主要部件保修3年
2. 耳机/音箱：保修1年
3. 显示器/配件：保修3年
4. 电池：保修6个月
5. 激活日期起算，需提供购买凭证

## 二、保修范围
1. 非人为损坏的性能故障
2. 产品质量问题
3. 符合国家三包规定的情形

## 三、不保修范围
1. 人为损坏、进水、摔坏
2. 私自拆卸、改装、越狱
3. 使用非原装配件
4. 正常使用磨损
5. 不可抗力造成的损坏

## 四、维修流程
1. 联系客服申请维修 → 提供故障描述和照片
2. 客服审核后提供维修编号
3. 寄修：快递到指定维修点，运费商家承担
4. 到店修：前往授权维修点
5. 维修完成后寄回或到店取

## 五、维修时效
1. 检测：收到商品后3-5个工作日
2. 维修：配件齐全情况下7-10个工作日
3. 返件：维修完成后1-3个工作日寄出',
'售后政策', 
'["保修", "维修", "质量", "三包"]',
'商品保修期限、范围、流程和维修时效说明。',
90, '系统管理员'),

-- 物流配送相关
('发货与物流时效', 
'# 发货与物流时效

## 一、发货时间
1. 工作日16:00前付款订单，当天发货
2. 工作日16:00后及周末订单，次日发货
3. 预售商品按页面显示时间发货
4. 大促期间可能延迟1-3天发货

## 二、配送时效
1. 普通快递：同城1-2天，省内2-3天，省外3-5天
2. 顺丰快递：全国1-3天
3. 偏远地区：新疆、西藏、青海等5-7天
4. 港澳台及海外：7-15天

## 三、物流查询
1. 网站查询：登录账号 → 我的订单 → 查看物流
2. 微信查询：关注公众号 → 绑定账号 → 物流查询
3. 快递官网查询：复制运单号到对应快递公司官网查询
4. 客服查询：提供订单号，客服协助查询

## 四、物流异常处理
1. 超时未发货：联系客服催促
2. 物流停滞：联系快递公司或客服查询
3. 包裹丢失：联系客服申请补发或退款
4. 配送异常：地址错误、联系不上等，及时联系客服修改',
'物流配送', 
'["发货", "物流", "快递", "配送", "时效"]',
'发货时间、物流时效、查询方式和异常处理说明。',
80, '系统管理员'),

-- 支付问题相关
('支付方式与限额', 
'# 支付方式与限额

## 一、支持的支付方式
1. 支付宝：支持余额、余额宝、花呗、信用卡
2. 微信支付：支持零钱、银行卡、信用卡
3. 银行卡支付：支持储蓄卡、信用卡
4. 京东白条：支持分期付款
5. 货到付款：部分商品支持，需现金或刷卡

## 二、支付限额
1. 支付宝：单笔最高5万元，日累计20万元
2. 微信支付：单笔最高5万元，日累计20万元
3. 银行卡：以发卡行限额为准
4. 信用卡：以银行授予额度为准

## 三、常见问题
1. 支付失败：检查网络、余额、支付限额
2. 重复扣款：不要重复提交，联系客服处理
3. 支付成功订单未生成：等待5-10分钟，或联系客服
4. 分期付款：部分商品支持3/6/12期免息
5. 组合支付：支持多种支付方式组合

## 四、支付安全
1. 不要在公共网络进行支付
2. 不要将验证码告诉他人
3. 定期修改支付密码
4. 开通支付提醒功能
5. 发现异常及时联系银行冻结账户',
'支付问题', 
'["支付", "支付宝", "微信", "银行卡", "限额"]',
'支持的支付方式、限额、常见问题和安全提示。',
70, '系统管理员'),

-- 会员权益相关
('会员等级与权益', 
'# 会员等级与权益

## 一、会员等级
1. 普通会员：注册即成为普通会员
2. 银卡会员：累计消费满1000元或连续购买3次
3. 金卡会员：累计消费满5000元或年度消费满3000元
4. 白金会员：累计消费满10000元或年度消费满5000元
5. 钻石会员：累计消费满30000元或年度消费满10000元

## 二、会员权益
1. 普通会员：基础积分、生日祝福
2. 银卡会员：额外9.8折、双倍积分、优先客服
3. 金卡会员：额外9.5折、三倍积分、专属客服
4. 白金会员：额外9.0折、四倍积分、VIP客服
5. 钻石会员：额外8.5折、五倍积分、专属顾问

## 三、积分规则
1. 消费积分：每消费1元获得1积分
2. 评价积分：每完成一次评价获得50积分
3. 签到积分：每日签到获得10积分
4. 分享积分：每成功分享获得20积分
5. 积分有效期：12个月

## 四、积分兑换
1. 积分商城：兑换商品、优惠券
2. 现金抵扣：100积分抵扣1元
3. 运费抵扣：可用积分抵扣运费
4. 特权兑换：兑换会员特权、服务',
'会员权益', 
'["会员", "等级", "权益", "积分"]',
'会员等级、权益、积分规则和兑换说明。',
60, '系统管理员'),

-- 常见问题
('如何查询订单状态', 
'# 如何查询订单状态

## 一、网站查询
1. 登录您的账号
2. 点击右上角"我的订单"
3. 找到需要查询的订单
4. 点击"查看详情"即可看到订单状态

## 二、APP查询
1. 打开APP并登录
2. 进入"我的"页面
3. 点击"我的订单"
4. 选择需要查询的订单查看状态

## 三、客服查询
1. 联系在线客服
2. 提供订单号
3. 客服将为您查询最新状态

## 四、常见订单状态说明
1. 待支付：订单已创建，等待付款
2. 已支付：付款成功，等待发货
3. 已发货：商品已发出，可查看物流
4. 已完成：订单已完成
5. 已取消：订单已取消
6. 退款中：正在处理退款',
'常见问题', 
'["订单", "查询", "状态", "物流"]',
'订单状态查询方法和常见状态说明。',
50, '系统管理员');

-- 5.4 初始化用户数据
INSERT INTO `user` (`id`, `username`, `nickname`, `phone`, `email`, `level`, `points`, `registration_time`) VALUES
(10001, 'zhangming', '张明', '13800138001', 'zhangming@example.com', 3, 1500, '2025-06-15 10:30:00'),
(10002, 'lina', '李娜', '13800138002', 'lina@example.com', 2, 800, '2025-08-20 14:15:00'),
(10003, 'wangqiang', '王强', '13800138003', 'wangqiang@example.com', 1, 300, '2025-10-25 16:40:00'),
(10004, 'lihua', '李华', '13800138004', 'lihua@example.com', 2, 1200, '2025-12-10 09:20:00'),
(10005, 'zhaowei', '赵伟', '13800138005', 'zhaowei@example.com', 4, 3500, '2026-01-15 11:30:00'),
(10006, 'testuser1', '测试用户1', '13800138006', 'test1@example.com', 1, 100, '2026-02-01 10:00:00'),
(10007, 'testuser2', '测试用户2', '13800138007', 'test2@example.com', 1, 200, '2026-02-02 11:00:00'),
(10008, 'admin', '管理员', '13800138008', 'admin@example.com', 5, 5000, '2026-01-01 00:00:00');

-- 5.5 初始化订单数据
INSERT INTO `order` (`order_number`, `user_id`, `user_name`, `total_amount`, `status`, `payment_method`, `shipping_address`, `contact_phone`, `create_time`) VALUES
('ORD202603080001', 10001, '张明', 6999.00, 3, '支付宝', '北京市朝阳区建国门外大街1号国贸大厦A座', '13800138001', '2026-03-01 14:30:00'),
('ORD202603080002', 10002, '李娜', 1899.00, 2, '微信支付', '上海市浦东新区陆家嘴环路100号上海中心大厦', '13800138002', '2026-03-05 09:15:00'),
('ORD202603080003', 10003, '王强', 2499.00, 1, '银联支付', '广州市天河区天河路208号天河城购物中心', '13800138003', '2026-03-07 11:20:00'),
('ORD202603080004', 10004, '李华', 899.00, 0, '微信支付', '深圳市南山区科技园科技中一路腾讯大厦', '13800138004', '2026-03-08 10:45:00'),
('ORD202603080005', 10005, '赵伟', 12999.00, 3, '支付宝', '杭州市西湖区文三路阿里巴巴园区', '13800138005', '2026-03-08 15:30:00'),
('ORD202603080006', 10001, '张明', 1599.00, 2, '支付宝', '北京市朝阳区建国门外大街1号国贸大厦A座', '13800138001', '2026-03-10 09:20:00'),
('ORD202603080007', 10002, '李娜', 2999.00, 3, '微信支付', '上海市浦东新区陆家嘴环路100号上海中心大厦', '13800138002', '2026-03-12 14:15:00');

-- 5.6 初始化售后数据
INSERT INTO `after_sales` (`service_number`, `order_id`, `user_id`, `type`, `reason`, `status`, `refund_amount`, `solution`, `create_time`) VALUES
('AS202603080001', 1, 10001, 1, '商品有质量问题', 2, 6999.00, '已退货退款', '2026-03-02 10:15:00'),
('AS202603080002', 2, 10002, 2, '发错颜色', 1, NULL, '已安排换货', '2026-03-06 14:30:00'),
('AS202603080003', 3, 10003, 5, '不想要了', 0, NULL, '待处理', '2026-03-08 09:20:00'),
('AS202603080004', 5, 10005, 3, '商品有故障', 1, NULL, '维修处理中', '2026-03-09 11:30:00'),
('AS202603080005', 7, 10002, 1, '尺寸不合适', 0, NULL, '待审核', '2026-03-13 10:45:00');

-- 5.11 初始化意图统计
INSERT INTO `chat_intent_stat` (`intent`, `stat_date`, `count`, `success_count`, `avg_response_time`) VALUES
('order_query', '2026-03-08', 1, 1, 3500),
('after_sales', '2026-03-08', 1, 1, 2800),
('complaint', '2026-03-08', 2, 2, 3000),
('logistics_query', '2026-03-08', 2, 2, 2550),
('member_query', '2026-03-08', 1, 1, 3900);

-- 5.12 初始化情感统计
INSERT INTO `chat_sentiment_stat` (`sentiment`, `stat_date`, `count`) VALUES
('neutral', '2026-03-08', 4),
('negative', '2026-03-08', 1),
('urgent', '2026-03-08', 1);

-- ============================================
-- 6. 创建索引优化
-- ============================================

-- 6.1 为chat_message表添加联合索引
ALTER TABLE `chat_message` ADD INDEX `idx_session_time` (`user_id`, `session_id`, `create_time`);
ALTER TABLE `chat_message` ADD INDEX `idx_user_sync` (`sync_status`, `embedding_status`);

-- 6.2 为chat_session表添加联合索引
ALTER TABLE `chat_session` ADD INDEX `idx_user_status_time` (`user_id`, `status`, `last_message_time`);

-- 6.3 为order表添加联合索引
ALTER TABLE `order` ADD INDEX `idx_user_status_time` (`user_id`, `status`, `create_time`);

-- 6.4 为after_sales表添加联合索引
ALTER TABLE `after_sales` ADD INDEX `idx_user_order_status` (`user_id`, `order_id`, `status`);

-- 6.5 为workflow_execution表添加联合索引
ALTER TABLE `workflow_execution` ADD INDEX `idx_session_status_time` (`session_id`, `status`, `start_time`);