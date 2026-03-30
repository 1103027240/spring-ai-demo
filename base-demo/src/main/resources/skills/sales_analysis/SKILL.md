---
name: sales_analysis
description: 销售分析技能，提供销售数据查询、业绩分析、客户行为分析功能
---

# 销售分析技能

## 功能概述
本技能提供完整的销售数据查询和分析功能，包括：
1. 销售业绩查询
2. 客户行为分析
3. 产品销售排行
4. 销售趋势分析
5. RFM客户细分

## 数据库表结构
注意：本技能不包含建表功能，只提供查询功能。请确保以下表已存在：
sql

-- 客户表
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    registration_date DATE,
    customer_tier VARCHAR(20) DEFAULT 'regular'
);

-- 订单表
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单明细表
    CREATE TABLE order_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL
);

-- 产品表 (与库存管理共享)
CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    unit_price DECIMAL(10,2),
    reorder_level INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

## 核心查询方法

### 1.月度销售业绩分析
sql

SELECT
    DATE_FORMAT(o.order_date, '%Y-%m') as sales_month,
    COUNT(DISTINCT o.order_id) as order_count,
    COUNT(DISTINCT o.customer_id) as customer_count,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as avg_order_value,
    SUM(oi.quantity) as total_units_sold
FROM orders o
INNER JOIN order_items oi ON o.order_id = oi.order_id
WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
    AND o.status = 'delivered'
GROUP BY DATE_FORMAT(o.order_date, '%Y-%m')
ORDER BY sales_month DESC;

### 2.产品销售排行榜
sql

SELECT
    p.product_id,
    p.product_name,
    p.category,
    COUNT(DISTINCT o.order_id) as times_ordered,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.quantity * oi.unit_price) as total_revenue,
    ROUND(SUM(oi.quantity * oi.unit_price) / SUM(oi.quantity), 2) as avg_selling_price
FROM products p
INNER JOIN order_items oi ON p.product_id = oi.product_id
INNER JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
    AND o.status = 'delivered'
GROUP BY p.product_id, p.product_name, p.category
ORDER BY total_revenue DESC
LIMIT 20;

### 3.客户价值分析 (RFM模型)
sql

WITH customer_rfm AS (
    SELECT
        c.customer_id,
        c.customer_name,
        c.customer_tier,
        DATEDIFF(CURDATE(), MAX(o.order_date)) as recency,
        COUNT(DISTINCT o.order_id) as frequency,
        SUM(o.total_amount) as monetary
    FROM customers c
    LEFT JOIN orders o ON c.customer_id = o.customer_id
    WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)
        AND o.status = 'delivered'
    GROUP BY c.customer_id, c.customer_name, c.customer_tier
)
SELECT
    customer_id,
    customer_name,
    customer_tier,
    recency,
    frequency,
    monetary,
    CASE
        WHEN recency <= 30 THEN '活跃客户'
        WHEN recency <= 90 THEN '一般客户'
        WHEN recency <= 180 THEN '沉睡客户'
        ELSE '流失客户'
    END as recency_segment,
    CASE
        WHEN frequency >= 10 THEN '高频客户'
        WHEN frequency >= 5 THEN '中频客户'
        WHEN frequency >= 1 THEN '低频客户'
        ELSE '新客户'
    END as frequency_segment,
    CASE
        WHEN monetary >= 10000 THEN '高价值客户'
        WHEN monetary >= 5000 THEN '中价值客户'
        WHEN monetary >= 1000 THEN '低价值客户'
        ELSE '潜在客户'
    END as monetary_segment
FROM customer_rfm
ORDER BY monetary DESC;

### 4.销售趋势分析
sql

WITH daily_sales AS (
    SELECT
        order_date,
        SUM(total_amount) as daily_revenue,
        COUNT(DISTINCT order_id) as daily_orders
    FROM orders
    WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)
        AND status = 'delivered'
    GROUP BY order_date
)
SELECT
    order_date,
    daily_revenue,
    daily_orders,
    AVG(daily_revenue) OVER (ORDER BY order_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) as weekly_avg_revenue,
    AVG(daily_orders) OVER (ORDER BY order_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) as weekly_avg_orders
FROM daily_sales
ORDER BY order_date DESC;

### 5.客户购买行为分析
sql

SELECT
    c.customer_id,
    c.customer_name,
    c.customer_tier,
    COUNT(DISTINCT o.order_id) as total_orders,
    SUM(o.total_amount) as total_spent,
    MIN(o.order_date) as first_order_date,
    MAX(o.order_date) as last_order_date,
    DATEDIFF(MAX(o.order_date), MIN(o.order_date)) as customer_lifetime_days,
    CASE
        WHEN COUNT(DISTINCT o.order_id) > 0 THEN DATEDIFF(MAX(o.order_date), MIN(o.order_date)) / COUNT(DISTINCT o.order_id)
        ELSE 0
    END as avg_days_between_orders
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
WHERE o.status = 'delivered'
GROUP BY c.customer_id, c.customer_name, c.customer_tier
HAVING total_orders > 0
ORDER BY total_spent DESC;

## 示例数据查询

### 1.查询所有订单
sql

SELECT
    o.order_id,
    c.customer_name,
    o.order_date,
    o.total_amount,
    o.status,
    o.payment_method
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
ORDER BY o.order_date DESC
LIMIT 50;

### 2.查询订单详情
sql

SELECT
    o.order_id,
    c.customer_name,
    p.product_name,
    oi.quantity,
    oi.unit_price,
    oi.quantity * oi.unit_price as item_total
FROM order_items oi
INNER JOIN orders o ON oi.order_id = o.order_id
INNER JOIN products p ON oi.product_id = p.product_id
INNER JOIN customers c ON o.customer_id = c.customer_id
WHERE o.order_id = ?
ORDER BY oi.item_id;

### 3.查询客户订单历史
sql

SELECT
    o.order_id,
    o.order_date,
    o.total_amount,
    o.status,
    o.payment_method
FROM orders o
WHERE o.customer_id = ?
ORDER BY o.order_date DESC;

### 4.查询产品销售历史
sql

SELECT
    o.order_date,
    c.customer_name,
    oi.quantity,
    oi.unit_price,
    oi.quantity * oi.unit_price as item_total
FROM order_items oi
INNER JOIN orders o ON oi.order_id = o.order_id
INNER JOIN customers c ON o.customer_id = c.customer_id
INNER JOIN products p ON oi.product_id = p.product_id
WHERE p.product_id = ?
ORDER BY o.order_date DESC;

## 高级分析查询

### 1.客户留存率分析
sql

WITH first_purchases AS (
    SELECT
        customer_id,
        MIN(order_date) as first_order_date
    FROM orders
    WHERE status = 'delivered'
    GROUP BY customer_id
),
cohort_sizes AS (
    SELECT
        DATE_FORMAT(first_order_date, '%Y-%m') as cohort_month,
        COUNT(DISTINCT customer_id) as cohort_size
    FROM first_purchases
    WHERE first_order_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
    GROUP BY DATE_FORMAT(first_order_date, '%Y-%m')
),
retention_data AS (
    SELECT
        DATE_FORMAT(fp.first_order_date, '%Y-%m') as cohort_month,
        TIMESTAMPDIFF(MONTH, fp.first_order_date, o.order_date) as months_since_first,
        COUNT(DISTINCT o.customer_id) as retained_customers
    FROM first_purchases fp
    INNER JOIN orders o ON fp.customer_id = o.customer_id
    WHERE o.status = 'delivered'
        AND o.order_date >= fp.first_order_date
    GROUP BY DATE_FORMAT(fp.first_order_date, '%Y-%m'), TIMESTAMPDIFF(MONTH, fp.first_order_date, o.order_date)
)
SELECT
    rd.cohort_month,
    cs.cohort_size,
    rd.months_since_first,
    rd.retained_customers,
    ROUND((rd.retained_customers * 100.0 / cs.cohort_size), 2) as retention_rate_percent
FROM retention_data rd
INNER JOIN cohort_sizes cs ON rd.cohort_month = cs.cohort_month
WHERE rd.months_since_first <= 6
ORDER BY rd.cohort_month DESC, rd.months_since_first;

### 2.交叉销售分析
sql

WITH product_pairs AS (
    SELECT
        oi1.product_id as product_a,
        oi2.product_id as product_b,
        COUNT(DISTINCT oi1.order_id) as times_bought_together
    FROM order_items oi1
    INNER JOIN order_items oi2 ON oi1.order_id = oi2.order_id
    WHERE oi1.product_id < oi2.product_id
    GROUP BY oi1.product_id, oi2.product_id
    HAVING COUNT(DISTINCT oi1.order_id) >= 5
)
SELECT
    p1.product_name as product_a_name,
    p2.product_name as product_b_name,
    pp.times_bought_together
FROM product_pairs pp
INNER JOIN products p1 ON pp.product_a = p1.product_id
INNER JOIN products p2 ON pp.product_b = p2.product_id
ORDER BY pp.times_bought_together DESC
LIMIT 20;

### 3.购物车分析
sql

SELECT
    COUNT(DISTINCT oi.order_id) as total_orders,
    AVG(items_per_order) as avg_items_per_order,
    AVG(order_value) as avg_order_value
FROM 
(
    SELECT
        oi.order_id,
        COUNT(DISTINCT oi.item_id) as items_per_order,
        SUM(oi.quantity * oi.unit_price) as order_value
    FROM order_items oi
    INNER JOIN orders o ON oi.order_id = o.order_id
    WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        AND o.status = 'delivered'
    GROUP BY oi.order_id
) as order_stats;

## 使用方法
1. 确保数据库连接配置正确
2. 在ReActAgent中使用`load_skill_through_path`工具加载此技能
3. 使用技能ID: `sales_analysis_mysql_agentscope_agentscope_skills`
4. 调用技能中的查询方法进行销售分析

## 工具说明
此技能不包含建表或数据修改功能，只提供查询和分析功能。所有查询均为只读操作，不会修改数据库数据。

## 性能提示
1. 建议为orders表创建索引：`CREATE INDEX idx_orders_order_date ON orders(order_date);`
2. 建议为orders表创建索引：`CREATE INDEX idx_orders_customer_id ON orders(customer_id);`
3. 建议为order_items表创建索引：`CREATE INDEX idx_order_items_order_id ON order_items(order_id);`
4. 建议定期归档历史数据以提高查询性能
5. 对于大数据量查询，建议使用分页查询