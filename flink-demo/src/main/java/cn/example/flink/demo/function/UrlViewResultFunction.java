package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UrlViewDto;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class UrlViewResultFunction extends ProcessWindowFunction<Long, UrlViewDto, String, TimeWindow> {

    @Override
    public void process(String url, Context context, Iterable<Long> elements, Collector<UrlViewDto> out) throws Exception {
        long start = context.window().getStart();
        long end = context.window().getEnd();
        Long count = elements.iterator().next();

        UrlViewDto result = UrlViewDto.builder()
                .url(url)
                .count(count)
                .windowStart(start)
                .windowEnd(end)
                .build();

        out.collect(result);
    }

}
