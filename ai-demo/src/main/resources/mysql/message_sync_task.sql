DROP TABLE IF EXISTS `message_sync_task`;
CREATE TABLE `message_sync_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `sync_type` tinyint NULL DEFAULT 1 COMMENT '同步类型(1:增量,2:全量,3:强制)',
  `last_message_id` bigint NULL DEFAULT NULL COMMENT '最后消息ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0:待处理,1:处理中,2:已完成,3:已失败,4:已取消)',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `max_retries` int NULL DEFAULT 3 COMMENT '最大重试次数',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `progress` int NULL DEFAULT 0 COMMENT '进度（0-100）',
  `total_messages` int NULL DEFAULT 0 COMMENT '总消息数',
  `processed_messages` int NULL DEFAULT 0 COMMENT '已处理消息数',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_pending`(`session_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_status_retry`(`status` ASC, `retry_count` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  INDEX `idx_updated_time`(`updated_time` ASC) USING BTREE,
  INDEX `idx_sync_type`(`sync_type` ASC) USING BTREE,
  INDEX `idx_session_status_time`(`session_id` ASC, `status` ASC, `created_time` DESC) USING BTREE,
  INDEX `idx_status_retry_time`(`status` ASC, `retry_count` ASC, `updated_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息同步任务表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
