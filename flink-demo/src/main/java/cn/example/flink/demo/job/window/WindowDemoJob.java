package cn.example.flink.demo.job.window;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import java.time.Duration;

public class WindowDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100);

        env.addSource(new ClickV2SourceFunction())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()))
                .map((MapFunction<UserVisitorDto, Tuple2<String, Long>>) e -> Tuple2.of(e.getUserId(), 1L))
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(e -> e.f0)
                //开窗
                //.window(TumblingEventTimeWindows.of(Duration.ofSeconds(10)))
                //.window(SlidingEventTimeWindows.of(Duration.ofSeconds(10), Duration.ofSeconds(2)))
                //.window(EventTimeSessionWindows.withGap(Duration.ofSeconds(3)))
                .window(GlobalWindows.create())
                //窗口函数
                .reduce((ReduceFunction<Tuple2<String, Long>>) (e2, e1) -> Tuple2.of(e1.f0, e2.f1 + e1.f1))
                .print();

        env.execute();
    }

}



