package cn.example.flink.demo.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

/**
 * DataSet处理
 */
@Slf4j
public class BatchWordCountJob {

    public static void main(String[] args) throws Exception {
        // 1、创建执行环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // 2、从文件读取数据
        String filePath = System.getProperty("user.dir") + "/flink-demo/input/words.txt";
        DataSource<String> dataSource = env.readTextFile(filePath);

        AggregateOperator<Tuple2<String, Long>> result = dataSource.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
            String[] words = line.split(" ");
            for (String word : words) {
                out.collect(new Tuple2<>(word, 1L));
            }})
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .groupBy(0)
                .sum(1);

        // 3、输出结果
        log.info("========== BatchWordCountJob 结果 ==========");
        result.print();
        log.info("====================================");
    }

}
