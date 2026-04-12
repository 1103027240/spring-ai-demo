package cn.example.flink.demo.job.source;

import cn.example.flink.demo.job.function.ParallelNumberSourceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 5、并行数据源
 */
public class ParallelNumberSourceJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<String> dataSourceStream = env.addSource(new ParallelNumberSourceFunction());
        dataSourceStream.print();

        env.execute();
    }

}
