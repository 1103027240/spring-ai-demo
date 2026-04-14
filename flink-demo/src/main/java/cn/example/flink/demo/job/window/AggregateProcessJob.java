package cn.example.flink.demo.job.window;

import cn.example.flink.demo.function.ClickV2SourceFunction;
import cn.example.flink.demo.function.UrlViewAggFunction;
import cn.example.flink.demo.function.UrlViewResultFunction;
import cn.example.flink.demo.param.UrlViewDto;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.util.OutputTag;
import java.time.Duration;

/**
 * 统计每个窗口UV
 */
public class AggregateProcessJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100);

        SingleOutputStreamOperator<UserVisitorDto> dataStream = env.addSource(new ClickV2SourceFunction())
                // 水位线
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofMillis(2))
                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()));
        dataStream.print("data");

        OutputTag<UserVisitorDto> outputTag = new OutputTag<>("lateData") {};
        SingleOutputStreamOperator<UrlViewDto> resultStream = dataStream.keyBy(UserVisitorDto::getUrl)
                // 开窗
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(10)))
                // 允许1min内迟到数据（该数据可以计算）
                .allowedLateness(Duration.ofMinutes(1))
                // 1min后迟到数据保存到侧输出流（该数据不能立即计算）
                .sideOutputLateData(outputTag)
                // 窗口函数
                .aggregate(new UrlViewAggFunction(), new UrlViewResultFunction());

        resultStream.print("result");
        resultStream.getSideOutput(outputTag).print("lateData");

        env.execute();
    }

}



