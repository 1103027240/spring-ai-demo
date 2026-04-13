package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import java.util.HashSet;
import java.util.Set;

public class AggregateSlidingFunction implements AggregateFunction<UserVisitorDto, Tuple2<Long, Set<String>>, Double> {


    @Override
    public Tuple2<Long, Set<String>> createAccumulator() {
        return Tuple2.of(0L, new HashSet<>());
    }

    /**
     * Tupl2：f0：PV访问量，f1：UV中每个用户
     */
    @Override
    public Tuple2<Long, Set<String>> add(UserVisitorDto dto, Tuple2<Long, Set<String>> tuple2) {
        tuple2.f1.add(dto.getUserId());
        return Tuple2.of(tuple2.f0 + 1, tuple2.f1);
    }

    @Override
    public Double getResult(Tuple2<Long, Set<String>> tuple2) {
        return (double) (tuple2.f0 / tuple2.f1.size());
    }

    @Override
    public Tuple2<Long, Set<String>> merge(Tuple2<Long, Set<String>> tuple2, Tuple2<Long, Set<String>> tuple1) {
        tuple2.f1.addAll(tuple1.f1);
        return Tuple2.of(tuple2.f0 + tuple1.f0, tuple2.f1);
    }

}
