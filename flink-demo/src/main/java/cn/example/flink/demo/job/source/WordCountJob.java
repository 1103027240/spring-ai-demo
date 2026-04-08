package cn.example.flink.demo.job.source;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.client.program.StreamContextEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 3、DataStream无界流处理
 */
@Slf4j
public class WordCountJob {

    public static void main(String[] args) throws Exception {
        // 1、创建执行环境
        StreamExecutionEnvironment env = StreamContextEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 2、从Socket读取数据（启动netcat服务器：nl -lk 9999）
        // 使用宿主机IP地址，通过ipconfig查看
        env.socketTextStream("host.docker.internal", 9999)
                .flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
                    String[] words = line.split(" ");
                    for (String word : words) {
                        out.collect(new Tuple2<>(word, 1L));
                    }
                })
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(tuple -> tuple.f0)
                .sum(1)
                .print();

        // 3、启动执行
        env.execute();
    }

}
