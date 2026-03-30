package cn.example.base.demo.config.skill;

import io.agentscope.core.skill.repository.ClasspathSkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Slf4j
@Configuration
public class DemoAgentSkillConfig {

    @Bean
    public ClasspathSkillRepository inventoryManagementSkillRepository() throws IOException {
        String resourcePath = "classpath:skills/inventory-management";
        return new ClasspathSkillRepository(resourcePath);
    }

    public MysqlSkilRepository mysqlSkilRepository() throws IOException {
        String resourcePath = "classpath:skills/mysql";
        return new MysqlSkilRepository(resourcePath);
    }

}
