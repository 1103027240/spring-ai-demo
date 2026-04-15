package cn.example.flink.demo.job.sink;

import cn.example.flink.demo.function.ClickSourceFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import com.mysql.cj.jdbc.MysqlXADataSource;
import org.apache.flink.connector.jdbc.JdbcExactlyOnceOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.core.datastream.sink.JdbcSink;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import javax.sql.XADataSource;

/**
 * 4、输出到Mysql
 */
public class JdbcSinkJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);  //Checkpoint配置（ExactlyOnce模式必需）

        env.addSource(new ClickSourceFunction(1000L)).sinkTo(createJdbcSink());
        env.execute();
    }

    public static JdbcSink createJdbcSink(){
        // 执行配置
        JdbcExecutionOptions jdbcExecutionOptions = createJdbcExecutionOptions();

        // MySQL XADataSource配置
        XADataSource xaDataSource = createXADataSource();

        // ExactlyOnce模式必须设置 maxRetries = 0
        // 使用 ON DUPLICATE KEY UPDATE 实现 Upsert（需确保 user_id 有唯一索引）
        return JdbcSink.<UserVisitorDto>builder()
                .withExecutionOptions(jdbcExecutionOptions)
                .withQueryStatement(
                        "INSERT INTO user_visitor (user_id, url, timestamp) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE url = VALUES(url), timestamp = VALUES(timestamp)",
                        (ps, data) -> {
                            ps.setString(1, data.getUserId());
                            ps.setString(2, data.getUrl());
                            ps.setLong(3, data.getTimestamp());
                        }
                )
                .buildExactlyOnce(JdbcExactlyOnceOptions.defaults(), () -> xaDataSource);
    }

    private static JdbcExecutionOptions createJdbcExecutionOptions(){
        return JdbcExecutionOptions.builder()
                .withMaxRetries(0)
                .build();
    }

    private static XADataSource createXADataSource() {
        MysqlXADataSource xaDataSource = new MysqlXADataSource();
        xaDataSource.setUrl("jdbc:mysql://localhost:3316/ai_demo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        xaDataSource.setUser("root");
        xaDataSource.setPassword("root");
        return xaDataSource;
    }

}
