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
 * DataStream有界流处理
 */
@Slf4j
public class BoundedWordCountJob {

    public static void main(String[] args) throws Exception {
        // 1、创建执行环境
        StreamExecutionEnvironment env = StreamContextEnvironment.getExecutionEnvironment();

        // 2、从文件读取数据
        String filePath = System.getProperty("user.dir") + "/flink-demo/input/words.txt";
        DataStreamSource<String> dataStreamSource = env.readTextFile(filePath);

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
        log.info("========== BoundedWordCountJob 结果 ==========");
        result.print();
        log.info("====================================");

        // 4、执行任务
        env.execute();
    }

}
