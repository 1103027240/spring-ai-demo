package cn.example.ai.demo.tools.routing;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CSDNTools {

    @Tool(name = "searchCSDN", description = "在CSDN上搜索技术文章、博客和教程")
    public String searchCSDN(@ToolParam(name = "query", description = "CSDN搜索代理") String query) {

        return """
               ## CSDN 搜索结果
               
               **查询**："%s"
               
               ### 热门文章
               
               1. **《Spring Boot从入门到实战》**
                  - 作者: 技术博主-小明
                  - 阅读量: 152,430
                  - 点赞数: 3,245
                  - 内容概要: 全面介绍Spring Boot核心功能，包含实战项目
               
               2. **《微服务架构设计模式详解》**
                  - 作者: 架构师-老王
                  - 阅读量: 89,567
                  - 收藏数: 4,891
                  - 内容概要: 深入讲解微服务设计模式和最佳实践
               
               3. **《Docker容器化部署完整指南》**
                  - 作者: DevOps专家-小李
                  - 阅读量: 67,892
                  - 评论数: 324
                  - 内容概要: 从基础到高级的Docker容器化部署教程
               
               ### 学习资源
               - 视频课程: "Java并发编程实战" (评分4.8/5)
               - 专栏订阅: "前端性能优化之道" (订阅数8,432)
               - 技术社区: 相关讨论帖1,234条
               
               *注：包含博客文章、学习资源和社区讨论*
               """.formatted(query);
    }

}
