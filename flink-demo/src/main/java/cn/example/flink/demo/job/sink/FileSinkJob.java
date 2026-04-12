package cn.example.flink.demo.job.sink;

import cn.example.flink.demo.job.init.UserVisitorBuilder;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import java.time.Duration;

/**
 * 1、输出到文件
 */
public class FileSinkJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String filePath = System.getProperty("user.dir") + "/flink-demo/output/";
        env.fromCollection(UserVisitorBuilder.buildUserVisitorDto())
                .addSink(StreamingFileSink.forRowFormat(new Path(filePath), new SimpleStringEncoder())
                        .withRollingPolicy(DefaultRollingPolicy.builder()
                                .withMaxPartSize(1024 * 1024 * 1024) //文件最大大小，超过则文件滚动
                                .withRolloverInterval(Duration.ofMinutes(5)) //文件打开最大时间，超过则文件滚动
                                .withInactivityInterval(Duration.ofMinutes(2)) //文件无数据写入最大时间，超过则文件滚动
                                .build())
                        .build());

        env.execute();
    }

}
