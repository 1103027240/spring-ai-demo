---
name: inventory_management
description: 库存管理技能，支持库存状态查询、补货提醒、库存变动分析、周转率分析、仓库分布、ABC分类、呆滞库存识别、入库记录查询、出库记录查询
---

## 表结构

products(产品表):
- product_id INT PK 自增 - 产品唯一标识
- product_name VARCHAR(255) - 产品名称
- category VARCHAR(100) - 产品类别
- unit_price DECIMAL(10,2) - 销售单价
- reorder_level INT - 补货阈值(库存低于此值需补货)

inventory(库存表):
- inventory_id INT PK 自增 - 库存记录ID
- product_id INT FK - 产品ID
- warehouse_id INT - 仓库(1主仓/2分仓/3备用仓)
- quantity INT - 库存数量

stock_in(入库表):
- record_id INT PK 自增 - 入库单号
- product_id INT FK - 产品ID
- quantity INT - 入库数量
- unit_cost DECIMAL(10,2) - 成本价
- supplier VARCHAR(255) - 供应商
- received_date DATE - 入库日期

stock_out(出库表):
- record_id INT PK 自增 - 出库单号
- product_id INT FK - 产品ID
- quantity INT - 出库数量
- order_id VARCHAR(100) - 订单号
- customer_name VARCHAR(255) - 客户名称
- shipped_date DATE - 出库日期

## 表关联

products.product_id = inventory.product_id
products.product_id = stock_in.product_id
products.product_id = stock_out.product_id

## 规则

1. 只生成SELECT，禁止INSERT/UPDATE/DELETE
2. 必须加LIMIT 10
3. 库存查询用LEFT JOIN保留无库存产品
4. NULL值用COALESCE(field,0)处理
5. 库存不足判断: quantity <= reorder_level
