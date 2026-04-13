package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.functions.AggregateFunction;

public class UrlViewAggFunction implements AggregateFunction<UserVisitorDto, Long, Long> {

    @Override
    public Long createAccumulator() {
        return 0L;
    }

    @Override
    public Long add(UserVisitorDto dto, Long count) {
        return count + 1;
    }

    @Override
    public Long getResult(Long count) {
        return count;
    }

    @Override
    public Long merge(Long count2, Long count1) {
        return count2 + count1;
    }

}
