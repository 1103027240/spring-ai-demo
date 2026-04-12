package cn.example.flink.demo.job.source;

import cn.example.flink.demo.job.function.RichDatabaseSourceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

/**
 * 6、富函数
 * 1）生命周期open/close方法
 * 2）可获取运行上下文：RuntimeContext getRuntimeContext();
 */
public class RichDatabaseSourceJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Row> dataSourceStream = env.addSource(new RichDatabaseSourceFunction());
        dataSourceStream.print();

        env.execute();
    }

}
