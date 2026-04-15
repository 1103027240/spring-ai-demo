package cn.example.flink.demo.job.window;

import cn.example.flink.demo.function.AggregateSlidingFunction;
import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import java.time.Duration;

/**
 * 统计每个窗口PV/UV平均访问率
 * PV：页面访问量
 * UV：独立访问量（某用户当天如果访问多次，UV只计算一次）
 */
public class AggregateSlidingJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<UserVisitorDto> dataStream = env.addSource(new ClickV2SourceFunction(1000L))
                // 水位线
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream.print("data");

        dataStream.keyBy(e -> true)
                // 开窗
                .window(SlidingEventTimeWindows.of(Duration.ofSeconds(10), Duration.ofSeconds(2)))
                // 窗口函数
                .aggregate(new AggregateSlidingFunction())
                .print("result");

        env.execute();
    }

}



