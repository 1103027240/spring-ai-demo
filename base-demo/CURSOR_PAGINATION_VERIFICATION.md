# 游标分页准确性验证

## 游标分页设计

### 游标结构
```
Base64编码: page|primarySortKey|secondarySortKey
```

### 游标语义
- **nextCursor**: 指向**下一页的第一条**数据
- **prevCursor**: 指向**上页的第一条**数据
- **游标页码**: 0=第1页，1=第2页，2=第3页...

## 测试场景

假设：pageSize=20，总数据100条（索引0-99），按score降序

---

### 第1页（首页）

**请求：**
```json
{
  "cursorDirection": "first",
  "pageSize": 20
}
```

**处理流程：**
1. isNextPage() = false, isPrevPage() = false, isFirstPage() = true
2. topK = 20
3. 查询向量库：返回索引 [0-99]（假设100条）
4. 排序后：sortedResults [0-99]
5. 返回数据：[0-19]

**返回：**
```json
{
  "data": [文档0, 文档1, ..., 文档19],  // 20条
  "nextCursor": "MQ|score_0.85|docId_100",  // Base64: "1|score_0.85|docId_100"
    // 指向索引20（第2页第一条）
    // 页码=1
  "prevCursor": null,
  "hasNext": true,
  "hasPrev": false
}
```

---

### 第2页（请求下一页）

**请求：**
```json
{
  "cursorDirection": "next",
  "backwardCursor": "MQ|score_0.85|docId_100",  // 第1页返回的nextCursor
  "pageSize": 20
}
```

**处理流程：**
1. isNextPage() = true
2. decodeCursor(backwardCursor): [1, "score_0.85", "docId_100"]
   - currentPageNum = 1
   - primaryCursor = "score_0.85"
   - secondCursor = "docId_100"
3. calculateTopK: topK = (1+1)*20 = 40
4. 查询向量库：返回索引 [0-39]（40条）
5. findCursorIndex: 找到索引20（匹配 "score_0.85|docId_100"）
6. startIndex = 20（从游标位置开始）
7. endIndex = min(20+20, 40) = 40
8. 返回数据：[20-39]

**返回：**
```json
{
  "data": [文档20, 文档21, ..., 文档39],  // 20条
  "nextCursor": "Mg|score_0.80|docId_120",  // Base64: "2|score_0.80|docId_120"
    // 指向索引40（第3页第一条）
    // 页码=2
  "prevCursor": "MQ|score_0.85|docId_100",  // 指向索引20（第2页第一条）
    // 页码=1
  "hasNext": true,
  "hasPrev": true
}
```

---

### 第3页（请求下一页）

**请求：**
```json
{
  "cursorDirection": "next",
  "backwardCursor": "Mg|score_0.80|docId_120",  // 第2页返回的nextCursor
  "pageSize": 20
}
```

**处理流程：**
1. isNextPage() = true
2. decodeCursor: [2, "score_0.80", "docId_120"]
   - currentPageNum = 2
3. calculateTopK: topK = (2+1)*20 = 60
4. 查询向量库：返回索引 [0-59]（60条）
5. findCursorIndex: 找到索引40（匹配 "score_0.80|docId_120"）
6. startIndex = 40
7. endIndex = min(40+20, 60) = 60
8. 返回数据：[40-59]

**返回：**
```json
{
  "data": [文档40, 文档41, ..., 文档59],  // 20条
  "nextCursor": "Mw|score_0.75|docId_140",  // 页码=3，指向索引60
  "prevCursor": "Mg|score_0.80|docId_120",  // 页码=2，指向索引40
  "hasNext": true,
  "hasPrev": true
}
```

---

### 第2页（从第3页返回上一页）

**请求：**
```json
{
  "cursorDirection": "prev",
  "forwardCursor": "Mg|score_0.80|docId_120",  // 第2页的prevCursor
  "pageSize": 20
}
```

**处理流程：**
1. isPrevPage() = true
2. decodeCursor: [2, "score_0.80", "docId_120"]
   - currentPageNum = 2
3. calculateTopK: topK = 2*20 = 40
4. 查询向量库：返回索引 [0-39]（40条）
5. findCursorIndex: 找到索引40（匹配 "score_0.80|docId_120"）
6. startIndex = max(0, 40-20) = 20
7. endIndex = 40
8. 返回数据：[20-39]

**返回：**
```json
{
  "data": [文档20, 文档21, ..., 文档39],  // 20条
  "nextCursor": "Mg|score_0.80|docId_120",  // 指向索引40（第3页第一条）
  "prevCursor": "MQ|score_0.85|docId_100",  // 指向索引20（第2页第一条）
  "hasNext": true,
  "hasPrev": true
}
```

---

## 数据准确性保证

### 1. 无重复数据
- 第1页：[0-19]
- 第2页：[20-39]
- 第3页：[40-59]
- 索引范围连续，无重叠

### 2. 无遗漏数据
- 每页连续获取20条
- 游标精确定位到正确索引
- subList()保证范围正确

### 3. 边界检查
- 游标找到失败 → 返回空列表
- startIndex >= endIndex → 返回空列表
- 游标索引越界 → 返回空列表
- 数据不足 → 返回空列表

### 4. 数据验证
- 检查空值和null
- 检查docId唯一性
- 检查排序顺序正确性
- 异常情况抛出RuntimeException

### 5. 详细日志
- 每个分页操作都有INFO日志
- 包含：页码、索引范围、数据量
- 便于问题排查和监控

---

## 关键修复点

### 修复前问题
1. **游标指向不明确**：nextCursor指向当前页最后一条，语义不清
2. **topK计算错误**：游标页码与topK不匹配
3. **缺少数据验证**：无空值、重复、排序检查
4. **边界处理不完善**：末尾判断不准确

### 修复后
1. **游标语义清晰**：nextCursor指向下一页第一条，prevCursor指向上页第一条
2. **topK计算正确**：根据游标页码准确计算需要的查询数量
3. **完整数据验证**：空值、唯一性、排序、边界全面检查
4. **完善边界处理**：各种边界情况都有保护

---

## 配置建议

### 开发环境
```yaml
customer:
  search:
    redundancy:
      enabled: true
      factor: 1.5      # 提高冗余，应对向量搜索不稳定
      maxExtra: 200
  maxPageLimit: 20
```

### 生产环境
```yaml
customer:
  search:
    redundancy:
      enabled: true
      factor: 1.2      # 平衡性能和准确性
      maxExtra: 100
  maxPageLimit: 10
```

---

## 监控指标

通过日志可以监控：
1. **游标命中率**：查看"游标未找到"的频率
2. **排序异常**：查看"排序顺序异常"的频率
3. **数据重复**：查看"存在重复docId"的频率
4. **分页耗时**：查看各页的查询时间
5. **数据一致性**：查看各页的数据是否连续
