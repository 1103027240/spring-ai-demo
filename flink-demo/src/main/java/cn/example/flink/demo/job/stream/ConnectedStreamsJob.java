package cn.example.flink.demo.job.stream;

import cn.example.flink.demo.function.DemoCoProcessFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.time.Duration;

/**
 * 3、Connect合流
 */
public class ConnectedStreamsJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Tuple3<String, String, Long>> appStream = env.fromData(
                        Tuple3.of("Order-4000", "app", 5000L),
                        Tuple3.of("Order-3000", "app", 7000L),
                        Tuple3.of("Order-2000", "app", 4000L),
                        Tuple3.of("Order-1000", "app", 1000L)
                )
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Tuple3<String, String, Long>>forBoundedOutOfOrderness(Duration.ofMillis(1))
                        .withTimestampAssigner((tuple3, recordTimestamp) -> tuple3.f2));

        SingleOutputStreamOperator<Tuple4<String, String, String, Long>> thirdPartyStream = env.fromData(
                        Tuple4.of("Order-2000", "thirdParty", "success", 24000L),
                        Tuple4.of("Order-1000", "thirdParty", "success", 11000L)
                )
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Tuple4<String, String, String, Long>>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((tuple3, recordTimestamp) -> tuple3.f3));

        appStream.connect(thirdPartyStream)
                .keyBy(e1 -> e1.f0, e2 -> e2.f0)
                .process(new DemoCoProcessFunction())
                .print("result");

        env.execute();
    }

}
