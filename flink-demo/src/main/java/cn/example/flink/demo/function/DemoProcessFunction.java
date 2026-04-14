package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class DemoProcessFunction extends ProcessFunction<UserVisitorDto, String> {

    @Override
    public void processElement(UserVisitorDto value, Context ctx, Collector<String> out) throws Exception {
        out.collect(value.toString());
        System.out.println(String.format("当前时间: %s, 旧水位线: %s", ctx.timestamp(), ctx.timerService().currentWatermark()));
    }

}
