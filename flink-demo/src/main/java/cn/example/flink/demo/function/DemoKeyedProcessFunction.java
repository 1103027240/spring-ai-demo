package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class DemoKeyedProcessFunction extends KeyedProcessFunction<String, UserVisitorDto, String> {

    private final long interval = 1000; //延迟1s

    @Override
    public void processElement(UserVisitorDto value, Context ctx, Collector<String> out) throws Exception {
        Long timestamp = ctx.timestamp();
        String result = String.format("分组key：%s，当前时间：%s，旧水位线：%s，当前数据：%s", ctx.getCurrentKey(), timestamp, ctx.timerService().currentWatermark(), value.toString());

        out.collect(result);
        ctx.timerService().registerEventTimeTimer(timestamp + interval); //注册事件时间触发器
    }

    /**
     * 触发器触发时且当前watermark >= 触发器触发时间调用，当前watermark=max(timestamp)-延迟时间
     * timestamp来源于上面processElement方法事件时间触发器触发时间
     */
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
        out.collect(String.format("分组Key：%s，触发器触发时间：%s", ctx.getCurrentKey(), timestamp));
    }

}
