package cn.example.flink.demo.job.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.Properties;

/**
 * 7、Kafka数据源
 */
public class KafkaSourceJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
        properties.setProperty("request.timeout.ms", "120000");      // 请求超时：2分钟
        properties.setProperty("default.api.timeout.ms", "120000");  // 默认 API 超时
        properties.setProperty("max.poll.interval.ms", "300000");    // 消费间隔超时：5分钟
        properties.setProperty("session.timeout.ms", "45000");       // 会话超时
        properties.setProperty("heartbeat.interval.ms", "3000");     // 心跳间隔
        properties.setProperty("auto.offset.reset", "earliest");     // 默认从最早开始

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setProperties(properties)
                .setTopics("test")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())  // 消费起点
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new SimpleStringSchema()))
                .build();

        env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "KafkaSource")
                .print();

        env.execute();
    }

}
