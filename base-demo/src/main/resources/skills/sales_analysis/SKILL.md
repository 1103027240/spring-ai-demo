---
name: sales_analysis
description: 销售分析技能
---

## 意图映射表

| 用户意图 | 关键词 | 对应查询 |
|---------|-------|---------|
| 销售业绩 | 销售、销售业绩、销售情况、月度销售 | 查询1 |
| 产品排行 | 产品排行、销量排名、销售排行、热销产品 | 查询2 |
| 客户分析 | 客户分析、RFM、客户价值、客户分类 | 查询3 |
| 销售趋势 | 销售趋势、趋势分析、销售走势 | 查询4 |
| 购买行为 | 购买行为、客户行为、消费行为 | 查询5 |
| 订单列表 | 订单、所有订单、订单列表 | 查询6 |
| 订单详情 | 订单详情、订单明细 | 查询7 |
| 客户订单 | 客户订单、订单历史、购买历史 | 查询8 |
| 产品销售 | 产品销售、销售历史 | 查询9 |
| 留存率 | 留存、留存率、客户留存 | 查询10 |
| 交叉销售 | 交叉销售、关联产品、搭配购买 | 查询11 |
| 购物车 | 购物车、客单价、平均订单 | 查询12 |

## 表结构

```
customers(客户表):
  customer_id      -- 客户ID
  customer_name    -- 客户名称
  email            -- 邮箱
  phone            -- 电话
  customer_tier    -- 客户等级

orders(订单表):
  order_id         -- 订单ID
  customer_id      -- 客户ID
  order_date       -- 订单日期
  total_amount     -- 订单金额
  status           -- 订单状态
  payment_method   -- 支付方式

order_items(订单明细表):
  item_id          -- 明细ID
  order_id         -- 订单ID
  product_id       -- 产品ID
  quantity         -- 数量
  unit_price       -- 单价

products(产品表):
  product_id       -- 产品ID
  product_name     -- 产品名称
  category         -- 分类
  unit_price       -- 单价
```

## 业务查询

### 查询1
```sql
SELECT DATE_FORMAT(o.order_date, '%Y-%m') as sales_month, COUNT(DISTINCT o.order_id) as order_count, COUNT(DISTINCT o.customer_id) as customer_count, SUM(o.total_amount) as total_revenue, AVG(o.total_amount) as avg_order_value, SUM(oi.quantity) as total_units_sold FROM orders o INNER JOIN order_items oi ON o.order_id = oi.order_id WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) AND o.status = 'delivered' GROUP BY DATE_FORMAT(o.order_date, '%Y-%m') ORDER BY sales_month DESC limit 10;
```

### 查询2
```sql
SELECT p.product_id, p.product_name, p.category, COUNT(DISTINCT o.order_id) as times_ordered, SUM(oi.quantity) as total_quantity_sold, SUM(oi.quantity * oi.unit_price) as total_revenue, ROUND(SUM(oi.quantity * oi.unit_price) / SUM(oi.quantity), 2) as avg_selling_price FROM products p INNER JOIN order_items oi ON p.product_id = oi.product_id INNER JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH) AND o.status = 'delivered' GROUP BY p.product_id, p.product_name, p.category ORDER BY total_revenue DESC limit 10;
```

### 查询3
```sql
WITH customer_rfm AS (SELECT c.customer_id, c.customer_name, c.customer_tier, DATEDIFF(CURDATE(), MAX(o.order_date)) as recency, COUNT(DISTINCT o.order_id) as frequency, SUM(o.total_amount) as monetary FROM customers c LEFT JOIN orders o ON c.customer_id = o.customer_id WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 365 DAY) AND o.status = 'delivered' GROUP BY c.customer_id, c.customer_name, c.customer_tier) SELECT customer_id, customer_name, customer_tier, recency, frequency, monetary, CASE WHEN recency <= 30 THEN '活跃客户' WHEN recency <= 90 THEN '一般客户' WHEN recency <= 180 THEN '沉睡客户' ELSE '流失客户' END as recency_segment, CASE WHEN frequency >= 10 THEN '高频客户' WHEN frequency >= 5 THEN '中频客户' WHEN frequency >= 1 THEN '低频客户' ELSE '新客户' END as frequency_segment, CASE WHEN monetary >= 10000 THEN '高价值客户' WHEN monetary >= 5000 THEN '中价值客户' WHEN monetary >= 1000 THEN '低价值客户' ELSE '潜在客户' END as monetary_segment FROM customer_rfm ORDER BY monetary DESC limit 10;
```

### 查询4
```sql
WITH daily_sales AS (SELECT order_date, SUM(total_amount) as daily_revenue, COUNT(DISTINCT order_id) as daily_orders FROM orders WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY) AND status = 'delivered' GROUP BY order_date) SELECT order_date, daily_revenue, daily_orders, AVG(daily_revenue) OVER (ORDER BY order_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) as weekly_avg_revenue, AVG(daily_orders) OVER (ORDER BY order_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) as weekly_avg_orders FROM daily_sales ORDER BY order_date DESC limit 10;
```

### 查询5
```sql
SELECT c.customer_id, c.customer_name, c.customer_tier, COUNT(DISTINCT o.order_id) as total_orders, SUM(o.total_amount) as total_spent, MIN(o.order_date) as first_order_date, MAX(o.order_date) as last_order_date, DATEDIFF(MAX(o.order_date), MIN(o.order_date)) as customer_lifetime_days, CASE WHEN COUNT(DISTINCT o.order_id) > 0 THEN DATEDIFF(MAX(o.order_date), MIN(o.order_date)) / COUNT(DISTINCT o.order_id) ELSE 0 END as avg_days_between_orders FROM customers c LEFT JOIN orders o ON c.customer_id = o.customer_id WHERE o.status = 'delivered' GROUP BY c.customer_id, c.customer_name, c.customer_tier HAVING total_orders > 0 ORDER BY total_spent DESC limit 10;
```

### 查询6
```sql
SELECT o.order_id, c.customer_name, o.order_date, o.total_amount, o.status, o.payment_method FROM orders o JOIN customers c ON o.customer_id = c.customer_id ORDER BY o.order_date DESC limit 10;
```

### 查询7
```sql
SELECT o.order_id, c.customer_name, p.product_name, oi.quantity, oi.unit_price, oi.quantity * oi.unit_price as item_total FROM order_items oi INNER JOIN orders o ON oi.order_id = o.order_id INNER JOIN products p ON oi.product_id = p.product_id INNER JOIN customers c ON o.customer_id = c.customer_id WHERE o.order_id = ? ORDER BY oi.item_id limit 10;
```

### 查询8
```sql
SELECT o.order_id, o.order_date, o.total_amount, o.status, o.payment_method FROM orders o WHERE o.customer_id = ? ORDER BY o.order_date DESC limit 10;
```

### 查询9
```sql
SELECT o.order_date, c.customer_name, oi.quantity, oi.unit_price, oi.quantity * oi.unit_price as item_total FROM order_items oi INNER JOIN orders o ON oi.order_id = o.order_id INNER JOIN customers c ON o.customer_id = c.customer_id INNER JOIN products p ON oi.product_id = p.product_id WHERE p.product_id = ? ORDER BY o.order_date DESC limit 10;
```

### 查询10
```sql
WITH first_purchases AS (SELECT customer_id, MIN(order_date) as first_order_date FROM orders WHERE status = 'delivered' GROUP BY customer_id), cohort_sizes AS (SELECT DATE_FORMAT(first_order_date, '%Y-%m') as cohort_month, COUNT(DISTINCT customer_id) as cohort_size FROM first_purchases WHERE first_order_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) GROUP BY DATE_FORMAT(first_order_date, '%Y-%m')), retention_data AS (SELECT DATE_FORMAT(fp.first_order_date, '%Y-%m') as cohort_month, TIMESTAMPDIFF(MONTH, fp.first_order_date, o.order_date) as months_since_first, COUNT(DISTINCT o.customer_id) as retained_customers FROM first_purchases fp INNER JOIN orders o ON fp.customer_id = o.customer_id WHERE o.status = 'delivered' AND o.order_date >= fp.first_order_date GROUP BY DATE_FORMAT(fp.first_order_date, '%Y-%m'), TIMESTAMPDIFF(MONTH, fp.first_order_date, o.order_date)) SELECT rd.cohort_month, cs.cohort_size, rd.months_since_first, rd.retained_customers, ROUND((rd.retained_customers * 100.0 / cs.cohort_size), 2) as retention_rate_percent FROM retention_data rd INNER JOIN cohort_sizes cs ON rd.cohort_month = cs.cohort_month WHERE rd.months_since_first <= 6 ORDER BY rd.cohort_month DESC, rd.months_since_first limit 10;
```

### 查询11
```sql
WITH product_pairs AS (SELECT oi1.product_id as product_a, oi2.product_id as product_b, COUNT(DISTINCT oi1.order_id) as times_bought_together FROM order_items oi1 INNER JOIN order_items oi2 ON oi1.order_id = oi2.order_id WHERE oi1.product_id < oi2.product_id GROUP BY oi1.product_id, oi2.product_id HAVING COUNT(DISTINCT oi1.order_id) >= 5) SELECT p1.product_name as product_a_name, p2.product_name as product_b_name, pp.times_bought_together FROM product_pairs pp INNER JOIN products p1 ON pp.product_a = p1.product_id INNER JOIN products p2 ON pp.product_b = p2.product_id ORDER BY pp.times_bought_together DESC limit 10;
```

### 查询12
```sql
SELECT COUNT(DISTINCT oi.order_id) as total_orders, AVG(items_per_order) as avg_items_per_order, AVG(order_value) as avg_order_value FROM (SELECT oi.order_id, COUNT(DISTINCT oi.item_id) as items_per_order, SUM(oi.quantity * oi.unit_price) as order_value FROM order_items oi INNER JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND o.status = 'delivered' GROUP BY oi.order_id) as order_stats limit 10;
```
