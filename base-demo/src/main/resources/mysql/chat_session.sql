DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话唯一标识',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `session_type` tinyint NULL DEFAULT 1 COMMENT '会话类型(1:咨询,2:售后,3:投诉,4:建议,5:其他)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(1:进行中,0:已结束,2:已转人工,3:等待回复)',
  `start_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `message_count` int NULL DEFAULT 0 COMMENT '消息数量',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `session_data` json NULL COMMENT '会话数据（JSON格式）',
  `avg_response_time` int NULL DEFAULT NULL COMMENT '平均响应时间（毫秒）',
  `satisfaction_score` tinyint NULL DEFAULT NULL COMMENT '满意度评分（1-5）',
  `tags` json NULL COMMENT '标签（JSON数组）',
  `channel` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'web' COMMENT '渠道(web,app,wechat,other)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_last_message_time`(`last_message_time` ASC) USING BTREE,
  INDEX `idx_session_type`(`session_type` ASC) USING BTREE,
  INDEX `idx_channel`(`channel` ASC) USING BTREE,
  INDEX `idx_user_status_time`(`user_id` ASC, `status` ASC, `last_message_time` ASC) USING BTREE,
  INDEX `idx_type_start_time`(`session_type` ASC, `start_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 88 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客服会话表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
