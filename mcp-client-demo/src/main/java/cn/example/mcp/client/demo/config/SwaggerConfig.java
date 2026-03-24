package cn.example.mcp.client.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger3.x配置
 * @author 11030
 */
@Configuration
public class SwaggerConfig {

    /**
     * 版本
     */
    private static final String API_VERSION = "1.0";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("archetype OpenApi")
                        .version(API_VERSION)
                        .description("archetype API 描述"));
    }

}

