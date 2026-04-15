package cn.example.flink.demo.job.stream;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.DemoProcessJoinFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.time.Duration;

/**
 * 5、interval join合流
 */
public class IntervalJoinJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<UserVisitorDto> userStream = env.addSource(new ClickV2SourceFunction(1000L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(1))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        userStream.print("data1");

        SingleOutputStreamOperator<UserVisitorDto> urlStream = env.addSource(new ClickV2SourceFunction(500L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(500))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        urlStream.print("data2");

        userStream.keyBy(e -> e.getUserId())
                .intervalJoin(urlStream.keyBy(e -> e.getUserId()))
                //基于水位线时间戳为参数，T2在[T1-10, T1 + 30]范围内数据与T1连接
                .between(Duration.ofSeconds(-10), Duration.ofSeconds(30))
                .process(new DemoProcessJoinFunction())
                .print("result");

        env.execute();
    }

}
