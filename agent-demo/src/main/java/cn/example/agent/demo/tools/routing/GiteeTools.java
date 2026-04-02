package cn.example.agent.demo.tools.routing;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GiteeTools {

    @Tool(name = "searchGitee", description = "在Gitee（码云）上搜索中文开源项目和代码")
    public String searchGitee(@ToolParam(description = "Gitee搜索代理") String query) {

        return """
               ## Gitee 搜索结果
               
               **查询**："%s"
               
               ### 精选项目
               
               1. **华为/OpenHarmony** (⭐ 8.9k)
                  - 描述: 华为开源的HarmonyOS基础能力开源项目
                  - 语言: C, C++, JavaScript
                  - 开源协议: Apache-2.0
               
               2. **deepin-community/desktop-base** (⭐ 324)
                  - 描述: Deepin桌面环境基础配置
                  - 语言: Shell, Python
                  - 所属组织: deepin-community
               
               3. **Tencent/ncnn** (⭐ 20.7k)
                  - 描述: 腾讯开源的高性能神经网络前向计算框架
                  - 语言: C++
                  - 应用领域: 移动端AI推理
               
               ### 企业开源
               - **阿里巴巴/arthas** - Java诊断工具
               - **百度飞桨/PaddleOCR** - OCR工具包
               
               *注：重点关注国内企业和社区的开源项目*
               """.formatted(query);
    }

}
