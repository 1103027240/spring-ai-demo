package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import java.sql.Timestamp;

public class AggregateTumblingFunction implements AggregateFunction<UserVisitorDto, Tuple2<Long, Long>, String> {

    /**
     * 窗口创建时调用
     * Tuple2：f0：每个用户组时间戳之和，f1：每个用户组元素个数
     */
    @Override
    public Tuple2<Long, Long> createAccumulator() {
        return Tuple2.of(0L, 0L);
    }

    /**
     * 元素进入窗口时调用
     */
    @Override
    public Tuple2<Long, Long> add(UserVisitorDto dto, Tuple2<Long, Long> tuple2) {
        return Tuple2.of(dto.getTimestamp() + tuple2.f0, tuple2.f1 + 1);
    }

    /**
     * 窗口触发时调用
     */
    @Override
    public String getResult(Tuple2<Long, Long> tuple2) {
        return new Timestamp(tuple2.f0 / tuple2.f1).toString();
    }

    /**
     * 多个Session窗口合并时调用
     */
    @Override
    public Tuple2<Long, Long> merge(Tuple2<Long, Long> tuple2, Tuple2<Long, Long> tuple1) {
        return Tuple2.of(tuple2.f0 + tuple1.f0, tuple2.f1 + tuple1.f1);
    }

}
