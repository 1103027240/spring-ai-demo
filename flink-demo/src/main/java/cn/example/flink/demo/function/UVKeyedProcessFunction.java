package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UrlViewDto;
import cn.hutool.core.collection.CollUtil;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UVKeyedProcessFunction extends KeyedProcessFunction<Long, UrlViewDto, String> {

    private Integer n;  //前N名
    private ListState<UrlViewDto> urlViewListState;  //状态列表

    public UVKeyedProcessFunction(Integer n) {
        this.n = n;
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        urlViewListState = getRuntimeContext().getListState(new ListStateDescriptor<>("urlViewListState", Types.POJO(UrlViewDto.class)));
    }

    @Override
    public void processElement(UrlViewDto value, Context ctx, Collector<String> out) throws Exception {
        // 数据存储到状态列表
        urlViewListState.add(value);

        // 注册触发器
        ctx.timerService().registerEventTimeTimer(ctx.getCurrentKey() + 1);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
        List<UrlViewDto> urlViewList = new ArrayList<>();
        urlViewListState.get().forEach(urlViewList::add);
        if (CollUtil.isEmpty(urlViewList)) {
            return;
        }

        // 按访问量降序排序
        urlViewList.sort((o1, o2) -> Long.compare(o2.getCount(), o1.getCount()));

        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("窗口：%s, time: %s \n", ctx.getCurrentKey(), new Timestamp(ctx.getCurrentKey())));

        // 取前N名
        int limit = Math.min(n, urlViewList.size());
        for (int i = 0; i < limit; i++) {
            UrlViewDto dto = urlViewList.get(i);
            buffer.append(String.format("No. %s  url：%s  访问量：%s \n", i + 1, dto.getUrl(), dto.getCount()));
        }
        out.collect(buffer.toString());
    }

}
