package cn.example.agent.demo.config;

import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import static com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.CreateOption.CREATE_IF_NOT_EXISTS;

/**
 * @author 11030
 */
@Configuration
public class MysqlSaverConfig {

    /**
     * 状态存储持久化方式: Mysql
     */
    @Bean
    public MysqlSaver mySqlSaver(DataSource dataSource) {
        return MysqlSaver.builder()
                .dataSource(dataSource) // 注入数据源
                .createOption(CREATE_IF_NOT_EXISTS) // 表不存在则自动创建
                .build(); // 使用默认StateSerializer（先转成二进制，然后再base64加密，序列化方法encodeState，反序列化方法decodeState）
    }

}
