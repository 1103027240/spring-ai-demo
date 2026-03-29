package cn.example.base.demo.tools.routing;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GitHubTools {

    @Tool(name = "searchGitHub", description = "在GitHub上搜索代码仓库、开源项目和代码片段")
    public String searchGitHub(@ToolParam(name = "query", description = "GitHub搜索代理") String query) {

        return """
               ## GitHub 搜索结果
               
               **查询**："%s"
               
               ### 相关仓库
               
               1. **spring-projects/spring-boot** (⭐ 73.5k)
                  - 描述: 用于构建独立的、生产级的Spring应用程序的框架
                  - 语言: Java
                  - 最近更新: 3天前
               
               2. **vitejs/vite** (⭐ 65.2k)
                  - 描述: 下一代前端工具，提供快速的开发服务器和优化的构建
                  - 语言: TypeScript
                  - 最近更新: 1天前
               
               3. **microsoft/vscode** (⭐ 162.3k)
                  - 描述: Visual Studio Code编辑器源代码
                  - 语言: TypeScript
                  - 最近更新: 5小时前
               
               ### 趋势项目
               - **vercel/next.js** - React全栈框架
               - **supabase/supabase** - 开源Firebase替代品
               
               *注：返回了3个最相关的仓库和2个趋势项目*
               """.formatted(query);
    }

}
