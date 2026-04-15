package cn.example.flink.demo.job.watermark;

import cn.example.flink.demo.function.ClickSourceFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import cn.example.flink.demo.strategy.CustomerWatermarkStrategy;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.time.Duration;

public class WatermarkStrategyJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100);

        env.addSource(new ClickSourceFunction(1000L))
                //1、无序流策略
//                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forBoundedOutOfOrderness(Duration.ofSeconds(3))
//                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()))

                //2、有序流策略
//                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserVisitorDto>forMonotonousTimestamps()
//                        .withTimestampAssigner((userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp()))

                //3、自定义生成水位线
                .assignTimestampsAndWatermarks(new CustomerWatermarkStrategy())

                .print();

        env.execute();
    }

}


