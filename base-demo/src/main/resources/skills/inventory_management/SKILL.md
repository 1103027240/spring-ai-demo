---
name: inventory_management
description: 库存管理技能
---

# 库存管理技能

## 意图映射表

| 用户意图 | 关键词 | 对应查询 |
|---------|-------|---------|
| 库存状态 | 库存、库存查询、库存情况、查看库存 | 查询1 |
| 补货提醒 | 补货、缺货、需要进货、进货提醒 | 查询2 |
| 库存变动 | 库存变动、出入库统计、进销存 | 查询3 |
| 周转率 | 周转、周转率、库存周转 | 查询4 |
| 仓库分布 | 仓库、仓库分布、各仓库库存 | 查询5 |
| 产品列表 | 产品、所有产品、产品信息 | 查询6 |
| 产品详情 | 产品详情、产品库存详情 | 查询7 |
| 入库记录 | 入库、进货记录、入库明细 | 查询8 |
| 出库记录 | 出库、发货记录、出库明细 | 查询9 |
| ABC分类 | ABC分类、库存分类、价值分类 | 查询10 |
| 呆滞库存 | 呆滞、滞销、不动销、积压 | 查询11 |

## 表结构

```
products(产品表):
  product_id      -- 产品ID
  product_name    -- 产品名称
  category        -- 分类
  unit_price      -- 单价
  reorder_level   -- 补货阈值

inventory(库存表):
  inventory_id    -- 库存ID
  product_id      -- 产品ID
  warehouse_id    -- 仓库ID
  quantity        -- 库存数量

stock_in(入库表):
  record_id       -- 记录ID
  product_id      -- 产品ID
  quantity        -- 入库数量
  supplier        -- 供应商
  received_date   -- 入库日期

stock_out(出库表):
  record_id       -- 记录ID
  product_id      -- 产品ID
  quantity        -- 出库数量
  order_id        -- 订单号
  shipped_date    -- 出库日期
```

## 业务查询

### 查询1: 库存状态
> 返回产品库存数量及状态（充足/需要补货/缺货）

```sql
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
ORDER BY inventory_status, total_quantity ASC
limit 10;
```

### 查询2: 补货提醒
> 返回库存低于补货阈值的产品

```sql
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
ORDER BY reorder_quantity DESC
limit 10;
```

### 查询3: 库存变动
> 返回近6个月的入库、出库及净变动统计

```sql
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
ORDER BY month DESC, net_change DESC
limit 10;
```

### 查询4: 周转率
> 返回近30天产品库存周转率

```sql
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
ORDER BY turnover_ratio DESC
limit 10;
```

### 查询5: 仓库分布
> 返回各仓库产品库存分布及库存价值

```sql
SELECT
    p.product_name,
    p.category,
    i.warehouse_id,
    i.quantity,
    p.unit_price,
    i.quantity * p.unit_price as inventory_value
FROM inventory i
JOIN products p ON i.product_id = p.product_id
ORDER BY i.warehouse_id, inventory_value DESC
limit 10;
```

### 查询6: 产品列表
> 返回所有产品基本信息

```sql
SELECT * 
FROM products 
ORDER BY product_name 
limit 10;
```

### 查询7: 产品详情
> 返回指定产品的库存详情（需替换?为产品ID）

```sql
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
WHERE p.product_id = ?
limit 10;
```

### 查询8: 入库记录
> 返回最近的入库记录

```sql
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
limit 10;
```

### 查询9: 出库记录
> 返回最近的出库记录

```sql
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
limit 10;
```

### 查询10: ABC分类
> 按库存价值进行ABC分类（A类占80%价值，B类占15%，C类占5%）

```sql
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
ORDER BY total_value DESC
limit 10;
```

### 查询11: 呆滞库存
> 返回超过90天未销售的库存产品

```sql
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
ORDER BY days_since_last_sale DESC
limit 10;
```
