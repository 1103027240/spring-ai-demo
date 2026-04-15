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

public class UrlViewKeyedProcessFunction extends KeyedProcessFunction<Long, UrlViewDto, String> {

    private Integer topN;  //前N名
    private Long windowSize;  //窗口大小
    private ListState<UrlViewDto> urlViewListState;  //状态列表

    public UrlViewKeyedProcessFunction(Integer topN, Long windowSize) {
        this.topN = topN;
        this.windowSize = windowSize;
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
        Long windowEnd = ctx.getCurrentKey();

        buffer.append(String.format("窗口：[%s ~ %s) \n", new Timestamp(windowEnd - windowSize), new Timestamp(windowEnd)));

        // 取前N名（支持并列排名）
        int rank = 0;           // 当前排名
        Long lastCount = null;  // 上一个访问量

        for (int i = 0; i < urlViewList.size(); i++) {
            UrlViewDto dto = urlViewList.get(i);

            if (lastCount == null || !lastCount.equals(dto.getCount())) {
                rank++;
            }

            // 排名超过N则停止
            if (rank > topN) {
                break;
            }

            buffer.append(String.format("No. %s  url：%s  访问量：%s \n", rank, dto.getUrl(), dto.getCount()));
            lastCount = dto.getCount();
        }

        out.collect(buffer.toString());
    }

}
