DROP TABLE IF EXISTS `workflow_execution`;
CREATE TABLE `workflow_execution`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `execution_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行唯一标识',
  `workflow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作流名称',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `input_data` json NULL COMMENT '输入数据（JSON格式）',
  `output_data` json NULL COMMENT '输出数据（JSON格式）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'RUNNING' COMMENT '状态(RUNNING,SUCCESS,FAILED,TIMEOUT,CANCELLED)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `node_results` json NULL COMMENT '节点执行结果（JSON格式）',
  `execution_path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行路径',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `max_retries` int NULL DEFAULT 3 COMMENT '最大重试次数',
  `timeout_seconds` int NULL DEFAULT 30 COMMENT '超时时间（秒）',
  `start_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `duration_ms` bigint NULL DEFAULT NULL COMMENT '执行时长（毫秒）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_workflow_name`(`workflow_name` ASC) USING BTREE,
  INDEX `idx_session_status_time`(`session_id` ASC, `status` ASC, `start_time` ASC) USING BTREE,
  INDEX `idx_user_start_time`(`user_id` ASC, `start_time` DESC) USING BTREE,
  INDEX `idx_status_time`(`status` ASC, `start_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 96 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工作流执行记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
