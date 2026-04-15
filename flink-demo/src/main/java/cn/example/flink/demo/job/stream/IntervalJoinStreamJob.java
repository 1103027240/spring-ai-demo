package cn.example.flink.demo.job.stream;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.util.Collector;
import java.sql.Timestamp;
import java.time.Duration;

public class IntervalJoinStreamJob {

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
                .between(Duration.ofSeconds(-10), Duration.ofSeconds(30))
                .process(new ProcessJoinFunction<UserVisitorDto, UserVisitorDto, String>() {
                    @Override
                    public void processElement(UserVisitorDto left, UserVisitorDto right, Context ctx, Collector<String> out) throws Exception {
                        UserVisitorDto earlier = left.getTimestamp() <= right.getTimestamp() ? left : right;
                        UserVisitorDto later = left.getTimestamp() > right.getTimestamp() ? left : right;

                        String result = String.format("%s ===》%s",
                                Tuple3.of(earlier.getUserId(), new Timestamp(earlier.getTimestamp()), earlier.getUrl()),
                                Tuple3.of(later.getUserId(), new Timestamp(later.getTimestamp()), later.getUrl()));
                        out.collect(result);
                    }
                }).print("result");

        env.execute();
    }

}
