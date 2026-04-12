package cn.example.flink.demo.job.sink;

import cn.example.flink.demo.job.param.UserVisitorDto;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import java.util.Properties;

/**
 * 2、输出到Kafka
 */
public class KafkaSinkJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
        properties.setProperty("auto.offset.reset", "earliest");  // 默认从最早开始

        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>("test1", new SimpleStringSchema(), properties);
        env.addSource(kafkaConsumer, "KafkaSource")
                .map(e -> {
                    String[] s = e.split(" ");
                    return new UserVisitorDto(s[0], s[1], Long.parseLong(s[2]));
                })
                .map(UserVisitorDto::toString)
                .addSink(new FlinkKafkaProducer("test2", new SimpleStringSchema(), properties));  // 输出到不同 topic

        env.execute();
    }

}
