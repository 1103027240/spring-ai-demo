---
name: inventory_management
description: 库存管理技能，提供产品库存查询、库存状态监控、库存分析功能
---

# 库存管理技能

## 功能概述
本技能提供完整的库存查询和分析功能，包括：
1. 库存状态查询
2. 库存周转率分析
3. 库存预警查询
4. 库存变动统计

## 数据库表结构设计
注意：本技能不包含建表功能，只提供查询功能。请确保以下表已存在：
sql

-- 产品表
CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    unit_price DECIMAL(10,2),
    reorder_level INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 库存表
CREATE TABLE inventory (
    inventory_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    warehouse_id INT,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 入库记录表
CREATE TABLE stock_in (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(10,2),
    supplier VARCHAR(255),
    received_date DATE,
    received_by VARCHAR(100)
);

-- 出库记录表
CREATE TABLE stock_out (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    order_id VARCHAR(100),
    customer_name VARCHAR(255),
    shipped_date DATE,
    shipped_by VARCHAR(100)
);

## 核心查询方法

### 1.查看当前库存状态
sql

SELECT
    p.product_id,
    p.product_name,
    p.category,
    p.unit_price,
    COALESCE(SUM(i.quantity), 0) as total_quantity,
    p.reorder_level,
    CASE
        WHEN COALESCE(SUM(i.quantity), 0) <= p.reorder_level THEN '需要补货'
        WHEN COALESCE(SUM(i.quantity), 0) = 0 THEN '缺货'
        ELSE '库存充足'
    END as inventory_status
FROM products p
LEFT JOIN inventory i ON p.product_id = i.product_id
GROUP BY p.product_id, p.product_name, p.category, p.unit_price, p.reorder_level
ORDER BY inventory_status, total_quantity ASC;

### 2.查询需要补货的产品
sql

SELECT
    p.product_id,
    p.product_name,
    p.category,
    p.unit_price,
    COALESCE(SUM(i.quantity), 0) as current_stock,
    p.reorder_level,
    p.reorder_level - COALESCE(SUM(i.quantity), 0) as reorder_quantity
FROM products p
LEFT JOIN inventory i ON p.product_id = i.product_id
GROUP BY p.product_id, p.product_name, p.category, p.unit_price, p.reorder_level
HAVING COALESCE(SUM(i.quantity), 0) <= p.reorder_level
ORDER BY reorder_quantity DESC;

### 3.月度库存变动报表
sql

SELECT
    DATE_FORMAT(date, '%Y-%m') as month,
    product_name,
    category,
    SUM(incoming) as total_incoming,
    SUM(outgoing) as total_outgoing,
    SUM(incoming) - SUM(outgoing) as net_change
FROM 
(
    SELECT
        si.received_date as date,
        p.product_name,
        p.category,
        si.quantity as incoming,
        0 as outgoing
    FROM stock_in si
    INNER JOIN products p ON si.product_id = p.product_id
    WHERE si.received_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
    UNION ALL
    SELECT
        so.shipped_date as date,
        p.product_name,
        p.category,
        0 as incoming,
        so.quantity as outgoing
    FROM stock_out so
    INNER JOIN products p ON so.product_id = p.product_id
    WHERE so.shipped_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
) as movements
GROUP BY DATE_FORMAT(date, '%Y-%m'), product_name, category
ORDER BY month DESC, net_change DESC;

### 4.库存周转率分析
sql

SELECT
    p.product_id,
    p.product_name,
    p.category,
    COALESCE(AVG(i.quantity), 0) as avg_inventory,
    COALESCE(SUM(so.quantity), 0) as total_sold,
    CASE
        WHEN COALESCE(AVG(i.quantity), 0) = 0 THEN 0
        ELSE COALESCE(SUM(so.quantity), 0) / COALESCE(AVG(i.quantity), 0)
    END as turnover_ratio
FROM products p
LEFT JOIN inventory i ON p.product_id = i.product_id
LEFT JOIN stock_out so ON p.product_id = so.product_id
AND so.shipped_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY p.product_id, p.product_name, p.category
ORDER BY turnover_ratio DESC;

### 5.仓库库存分布查询
sql

SELECT
    p.product_name,
    p.category,
    i.warehouse_id,
    i.quantity,
    p.unit_price,
    i.quantity * p.unit_price as inventory_value
FROM inventory i
JOIN products p ON i.product_id = p.product_id
ORDER BY i.warehouse_id, inventory_value DESC;

## 示例数据查询

### 1.查询所有产品
sql

SELECT * FROM products ORDER BY product_name;

### 2.查询产品库存详情
sql

SELECT
    p.product_id,
    p.product_name,
    p.category,
    p.unit_price,
    i.warehouse_id,
    i.quantity,
    i.last_updated
FROM products p
LEFT JOIN inventory i ON p.product_id = i.product_id
WHERE p.product_id = ?;

### 3.查询入库记录
sql

SELECT
    si.record_id,
    p.product_name,
    si.quantity,
    si.unit_cost,
    si.supplier,
    si.received_date,
    si.received_by
FROM stock_in si
INNER JOIN products p ON si.product_id = p.product_id
ORDER BY si.received_date DESC
LIMIT 50;

### 4.查询出库记录
sql

SELECT
    so.record_id,
    p.product_name,
    so.quantity,
    so.order_id,
    so.customer_name,
    so.shipped_date,
    so.shipped_by
FROM stock_out so
INNER JOIN products p ON so.product_id = p.product_id
ORDER BY so.shipped_date DESC
LIMIT 50;

## 高级分析查询

### 1. ABC库存分类分析
sql

WITH inventory_value AS (
    SELECT
        p.product_id,
        p.product_name,
        p.category,
        COALESCE(SUM(i.quantity), 0) as total_quantity,
        p.unit_price,
        COALESCE(SUM(i.quantity), 0) * p.unit_price as total_value
    FROM products p
    LEFT JOIN inventory i ON p.product_id = i.product_id
    GROUP BY p.product_id, p.product_name, p.category, p.unit_price
),
cumulative AS (
    SELECT
        product_id,
        product_name,
        category,
        total_value,
        SUM(total_value) OVER (ORDER BY total_value DESC) as running_total,
        SUM(total_value) OVER () as grand_total
    FROM inventory_value
)
SELECT
    product_id,
    product_name,
    category,
    total_value,
    ROUND((total_value / grand_total) * 100, 2) as percentage,
    ROUND((running_total / grand_total) * 100, 2) as cumulative_percentage,
    CASE
        WHEN (running_total / grand_total) <= 0.8 THEN 'A类'
        WHEN (running_total / grand_total) <= 0.95 THEN 'B类'
        ELSE 'C类'
    END as abc_class
FROM cumulative
ORDER BY total_value DESC;

### 2. 呆滞库存识别
sql

SELECT
    p.product_id,
    p.product_name,
    p.category,
    COALESCE(SUM(i.quantity), 0) as current_stock,
    MAX(so.shipped_date) as last_sale_date,
    DATEDIFF(CURDATE(), MAX(so.shipped_date)) as days_since_last_sale
FROM products p
LEFT JOIN inventory i ON p.product_id = i.product_id
LEFT JOIN stock_out so ON p.product_id = so.product_id
WHERE so.shipped_date IS NOT NULL
GROUP BY p.product_id, p.product_name, p.category
HAVING DATEDIFF(CURDATE(), MAX(so.shipped_date)) > 90 AND COALESCE(SUM(i.quantity), 0) > 0
ORDER BY days_since_last_sale DESC;

## 使用方法
1. 确保数据库连接配置正确
2. 在ReActAgent中使用`load_skill_through_path`工具加载此技能
3. 使用技能ID: `inventory_management_classpath-skills`
4. 调用技能中的查询方法进行库存分析

## 工具说明
此技能不包含建表或数据修改功能，只提供查询和分析功能。所有查询均为只读操作，不会修改数据库数据。

## 性能提示
1. 建议为products表创建索引：`CREATE INDEX idx_products_category ON products(category);`
2. 建议为inventory表创建索引：`CREATE INDEX idx_inventory_product_id ON inventory(product_id);`
3. 建议为stock_in和stock_out表创建日期索引
4. 定期清理历史数据以提高查询性能




