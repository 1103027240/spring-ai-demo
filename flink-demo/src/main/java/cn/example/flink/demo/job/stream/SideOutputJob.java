package cn.example.flink.demo.job.stream;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.DemoSideOutputProcessFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;
import java.time.Duration;

/**
 * 1、output分流
 */
public class SideOutputJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<UserVisitorDto> dataStream = env.addSource(new ClickV2SourceFunction(1000L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream.print("data");

        OutputTag<Tuple3<String, String, Long>> user1OutputTag = new OutputTag<>("user1") {};
        OutputTag<Tuple3<String, String, Long>> user2OutputTag = new OutputTag<>("user2") {};
        SingleOutputStreamOperator<String> resultStream = dataStream.process(new DemoSideOutputProcessFunction(user1OutputTag, user2OutputTag));

        resultStream.print("result");  //主输出流
        resultStream.getSideOutput(user1OutputTag).print("user1");  //侧输出流
        resultStream.getSideOutput(user2OutputTag).print("user2");  //侧输出流

        env.execute();
    }

}
