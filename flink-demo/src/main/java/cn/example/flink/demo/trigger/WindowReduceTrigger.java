package cn.example.flink.demo.trigger;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;

public class WindowReduceTrigger extends Trigger<Tuple2<String, Long>, GlobalWindow> {

    private final long interval = 3000; //延迟3s

    /**
     * 元素进入窗口时调用
     */
    @Override
    public TriggerResult onElement(Tuple2<String, Long> element, long timestamp, GlobalWindow window, TriggerContext ctx) throws Exception {
        System.out.println(String.format("当前分组元素: (%s,%s), 当前时间: %s, 旧水位线: %s", element.f0, element.f1, timestamp, ctx.getCurrentWatermark()));
        ctx.registerEventTimeTimer(timestamp + interval); //注册事件时间触发器
        return TriggerResult.CONTINUE;
    }

    /**
     * 事件时间触发器触发时且当前watermark >= 事件时间触发器触发时间调用，当前watermark=max(timestamp)-延迟时间
     * timestamp来源于上面onElement方法注册触发器触发时间
     * timestamp ==》找到含该timestamp列表数据 ==》找到分组key列表数据 ==》TriggerResult.FIRE触发计算，执行reduce方法
     */
    @Override
    public TriggerResult onEventTime(long timestamp, GlobalWindow window, TriggerContext ctx) throws Exception {
        System.out.println(String.format("事件时间触发器触发时间: %s, 旧水位线: %s", timestamp, ctx.getCurrentWatermark()));
        System.out.println("======================================================================================");
        return TriggerResult.FIRE;
    }

    /**
     * 处理时间触发器触发时且当前watermark >= 处理时间触发器触发时间调用，当前watermark=max(timestamp)-延迟时间
     * timestamp来源于上面onElement方法注册触发器触发时间
     * timestamp ==》找到含该timestamp列表数据 ==》找到分组key列表数据 ==》TriggerResult.FIRE触发计算，执行reduce方法
     */
    @Override
    public TriggerResult onProcessingTime(long timestamp, GlobalWindow window, TriggerContext ctx) throws Exception {
        System.out.println(String.format("处理时间触发器触发时间: %s, 旧水位线: %s", timestamp, ctx.getCurrentWatermark()));
        System.out.println("======================================================================================");
        return TriggerResult.CONTINUE;
    }

    /**
     * 窗口销毁时调用
     */
    @Override
    public void clear(GlobalWindow window, TriggerContext ctx) throws Exception {

    }

}
