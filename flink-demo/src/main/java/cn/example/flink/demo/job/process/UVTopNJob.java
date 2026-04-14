package cn.example.flink.demo.job.process;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.UVKeyedProcessFunction;
import cn.example.flink.demo.function.UrlViewAggFunction;
import cn.example.flink.demo.function.UrlViewResultFunction;
import cn.example.flink.demo.param.UrlViewDto;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import java.time.Duration;

/**
 * 统计每个窗口前2名UV
 */
public class UVTopNJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100);

        SingleOutputStreamOperator<UserVisitorDto> dataStream = env.addSource(new ClickV2SourceFunction())
                // 水位线
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream.print("data");

        long windowSize = 10;
        int topN = 2;

        // 统计每个窗口UV
        SingleOutputStreamOperator<UrlViewDto> aggregateStream = dataStream.keyBy(UserVisitorDto::getUrl)
                // 开窗
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(windowSize)))
                // 窗口函数
                .aggregate(new UrlViewAggFunction(), new UrlViewResultFunction());
        aggregateStream.print("aggregate");

        // 获取每个窗口前2名UV
        aggregateStream.keyBy(UrlViewDto::getWindowEnd)
                        .process(new UVKeyedProcessFunction(topN, windowSize * 1000))
                                .print("result");

        env.execute();
    }

}



