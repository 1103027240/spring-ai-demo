package cn.example.flink.demo.job.stream;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import java.time.Duration;

public class UnionJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<UserVisitorDto> dataStream1 = env.addSource(new ClickV2SourceFunction(1000L))
                // 水位线
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(1))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream1.print("data1");

        SingleOutputStreamOperator<UserVisitorDto> dataStream2 = env.addSource(new ClickV2SourceFunction(2000L))
                // 水位线
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream2.print("data2");

        dataStream1.union(dataStream2).process(new ProcessFunction<UserVisitorDto, String>() {
            @Override
            public void processElement(UserVisitorDto value, Context ctx, Collector<String> out) throws Exception {
                System.out.println(String.format("当前数据：%s，当前时间：%s，旧水位线：%s", value.toString(), ctx.timestamp(), ctx.timerService().currentWatermark()));
                out.collect(value.toString());
            }
        }).print("result");

        env.execute();
    }

}
