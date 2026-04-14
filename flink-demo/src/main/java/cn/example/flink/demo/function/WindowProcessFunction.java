package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * WindowProcessFunction：既是窗口函数，又是处理函数
 */
public class WindowProcessFunction extends ProcessWindowFunction<UserVisitorDto, String, Boolean, TimeWindow> {

    @Override
    public void process(Boolean aBoolean, Context context, Iterable<UserVisitorDto> elements, Collector<String> out) throws Exception {
        long start = context.window().getStart();
        long end = context.window().getEnd();

        Set<String> set = new HashSet<>();
        for(UserVisitorDto dto : elements){
            set.add(dto.getUserId());
        }

        String result = String.format("窗口：[%s ~ %s]，UV值：%s", new Timestamp(start), new Timestamp(end), set.size());
        out.collect(result);
    }

}
