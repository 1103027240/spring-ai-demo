package cn.example.flink.demo.job.process;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.DemoKeyedProcessFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.time.Duration;

public class DemoKeyedProcessFunctionJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<UserVisitorDto> dataStream = env.addSource(new ClickV2SourceFunction(1000L))
                // 水位线
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream.print("data");

        dataStream.keyBy(UserVisitorDto::getUserId)
                .process(new DemoKeyedProcessFunction())
                .print("result");

        env.execute();
    }

}



