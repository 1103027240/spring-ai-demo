package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class DemoUnionProcessFunction extends ProcessFunction<UserVisitorDto, String> {

    @Override
    public void processElement(UserVisitorDto value, Context ctx, Collector<String> out) throws Exception {
        System.out.println(String.format("当前数据：%s，当前时间：%s，当前水位线：%s", value.toString(), ctx.timestamp(), ctx.timerService().currentWatermark()));
        out.collect(value.toString());
    }

}
