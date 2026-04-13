package cn.example.flink.demo.job.window;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.AggregateTumblingFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import java.time.Duration;

/**
 * 统计每个窗口每个用户，页面访问平均时间
 */
public class AggregateTumblingJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100);

        SingleOutputStreamOperator<UserVisitorDto> dataStream = env.addSource(new ClickV2SourceFunction())
                // 水位线
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream.print("data");

        dataStream.keyBy(UserVisitorDto::getUserId)
                // 开窗
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(10)))
                // 窗口函数
                .aggregate(new AggregateTumblingFunction())
                .print("result");

        env.execute();
    }

}



