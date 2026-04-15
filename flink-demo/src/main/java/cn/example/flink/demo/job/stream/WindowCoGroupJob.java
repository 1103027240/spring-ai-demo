package cn.example.flink.demo.job.stream;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.DemoCoGroupFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import java.time.Duration;

/**
 * 5、Window CoGroup合流
 */
public class WindowCoGroupJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<UserVisitorDto> dataStream1 = env.addSource(new ClickV2SourceFunction(1000L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(1))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream1.print("data1");

        SingleOutputStreamOperator<UserVisitorDto> dataStream2 = env.addSource(new ClickV2SourceFunction(500L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(500))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream2.print("data2");

        dataStream1.coGroup(dataStream2)
                .where(UserVisitorDto::getUserId)
                .equalTo(UserVisitorDto::getUserId)
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(10)))
                .apply(new DemoCoGroupFunction())
                .print("result");

        env.execute();
    }

}
