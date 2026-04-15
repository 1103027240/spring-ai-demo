package cn.example.flink.demo.job.stream;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.DemoUnionProcessFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.time.Duration;

/**
 * 2、Union合流
 */
public class UnionJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<UserVisitorDto> dataStream1 = env.addSource(new ClickV2SourceFunction(1000L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(1))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream1.print("data1");

        SingleOutputStreamOperator<UserVisitorDto> dataStream2 = env.addSource(new ClickV2SourceFunction(2000L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream2.print("data2");

        dataStream1.union(dataStream2)
                .process(new DemoUnionProcessFunction())
                .print("result");

        env.execute();
    }

}
