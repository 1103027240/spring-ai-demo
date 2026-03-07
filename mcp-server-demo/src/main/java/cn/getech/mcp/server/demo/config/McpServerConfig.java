package cn.getech.mcp.server.demo.config;

import cn.getech.mcp.server.demo.service.impl.ProductServiceImpl;
import cn.getech.mcp.server.demo.service.impl.WeatherServiceImpl;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 11030
 */
@Configuration
public class McpServerConfig {

    /**
     * 工具方法暴露给mcp client调用
     * @return
     */
    @Bean
    public ToolCallbackProvider mcpServerTools(ProductServiceImpl productServiceImpl,
                                             WeatherServiceImpl weatherServiceImpl){
        return MethodToolCallbackProvider.builder()
                .toolObjects(productServiceImpl, weatherServiceImpl)
                .build();
    }

}
