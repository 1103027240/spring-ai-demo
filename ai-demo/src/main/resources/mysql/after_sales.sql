DROP TABLE IF EXISTS `after_sales`;
CREATE TABLE `after_sales`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  `service_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_item_id` bigint NULL DEFAULT NULL COMMENT '订单商品ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '售后类型(1:退货,2:换货,3:维修,4:退款)',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '售后原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0:待处理,1:处理中,2:已完成,3:已关闭,4:已取消)',
  `refund_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '退款金额',
  `solution` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解决方案',
  `processor_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `process_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `attachments` json NULL COMMENT '附件（JSON数组）',
  `customer_feedback` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户反馈',
  `satisfaction_score` tinyint NULL DEFAULT NULL COMMENT '满意度评分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_service_number`(`service_number` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_processor_id`(`processor_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_order_status`(`user_id` ASC, `order_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '售后表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
