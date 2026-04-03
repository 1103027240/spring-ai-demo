# MCP 协议集成

## 概述

MCP（Model Context Protocol）是一个标准化的协议，用于在大语言模型和外部工具/数据源之间建立连接。本项目实现了完整的 MCP 客户端和服务端。

## 模块说明

### MCP 客户端（mcp-client-demo）

用于连接外部 MCP 服务器，调用其提供的工具和资源。

**核心目录**：
```
mcp-client-demo/
├── config/              # MCP 客户端配置
├── controller/          # REST 接口
└── service/             # MCP 调用服务
```

### MCP 服务端（mcp-server-demo）

提供 MCP 协议服务，暴露工具供客户端调用。

**核心目录**：
```
mcp-server-demo/
├── config/              # MCP 服务端配置
├── dto/                 # 数据传输对象
└── service/             # 工具服务实现
```

## 配置说明

### 客户端配置

在 `application.yml` 中配置 MCP 服务器连接：

```yaml
mcp:
  client:
    servers:
      - name: demo-server
        url: http://localhost:8081/mcp
```

### 服务端配置

定义可暴露的工具：

```java
@McpTool
public class DemoTool {
    
    @McpFunction(description = "示例工具功能")
    public String execute(String input) {
        return "处理结果: " + input;
    }
}
```

## 使用场景

1. **工具调用** - LLM 通过 MCP 调用外部工具
2. **资源访问** - 访问外部数据源
3. **提示词模板** - 共享提示词模板
4. **采样** - LLM 请求客户端进行采样

## API 接口

| 端点 | 方法 | 说明 |
|------|------|------|
| `/mcp/tools` | GET | 获取可用工具列表 |
| `/mcp/call` | POST | 调用指定工具 |
| `/mcp/resources` | GET | 获取资源列表 |

## 相关配置文件

- `ai-demo/resources/mcp-server.json` - MCP 服务器配置
