---
name: sales_analysis
description: 销售分析技能，支持销售业绩统计、产品排行榜、客户RFM分析、销售趋势分析、购买行为分析、订单查询、客户订单历史、产品销售历史、留存率分析、交叉销售分析
---

## 表结构

customers(客户表):
- customer_id INT PK 自增 - 客户唯一标识
- customer_name VARCHAR(255) - 客户名称
- email VARCHAR(255) - 邮箱
- phone VARCHAR(50) - 电话
- registration_date DATE - 注册日期
- customer_tier VARCHAR(20) - 等级(regular/silver/gold/platinum)

orders(订单表):
- order_id INT PK 自增 - 订单ID
- customer_id INT FK - 客户ID
- order_date DATE - 订单日期
- total_amount DECIMAL(10,2) - 订单金额
- status VARCHAR(20) - 状态(pending/processing/shipped/delivered/cancelled)
- payment_method VARCHAR(50) - 支付方式

order_items(订单明细表):
- item_id INT PK 自增 - 明细ID
- order_id INT FK - 订单ID
- product_id INT FK - 产品ID
- quantity INT - 数量
- unit_price DECIMAL(10,2) - 单价

products(产品表):
- product_id INT PK 自增 - 产品ID
- product_name VARCHAR(255) - 产品名称
- category VARCHAR(100) - 产品类别
- unit_price DECIMAL(10,2) - 销售单价

## 表关联

customers.customer_id = orders.customer_id
orders.order_id = order_items.order_id
products.product_id = order_items.product_id

## 规则

1. 只生成SELECT，禁止INSERT/UPDATE/DELETE
2. 必须加LIMIT 10
3. 统计销售额必须加status='delivered'
4. NULL值用COALESCE(field,0)处理
5. 多表关联用JOIN并使用别名简化
