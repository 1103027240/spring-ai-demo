package cn.example.flink.demo.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.client.program.StreamContextEnvironment;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * DataStream无界流处理
 */
@Slf4j
public class WordCountJob {

    public static void main(String[] args) throws Exception {
        // 1、创建执行环境
        StreamExecutionEnvironment env = StreamContextEnvironment.getExecutionEnvironment();

        // 2、从Socket读取数据（启动netcat服务器：nl -lk 9999）
        DataStreamSource<String> dataStreamSource = env.socketTextStream("127.0.0.1", 9999);

        SingleOutputStreamOperator<Tuple2<String, Long>> result = dataStreamSource.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
                    String[] words = line.split(" ");
                    for (String word : words) {
                        out.collect(new Tuple2<>(word, 1L));
                    }
                })
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(tuple -> tuple.f0)
                .sum(1);

        // 3、输出结果
        log.info("========== WordCountJob 结果 ==========");
        result.print();
        log.info("====================================");

        // 4、执行任务
        env.execute();
    }

}
