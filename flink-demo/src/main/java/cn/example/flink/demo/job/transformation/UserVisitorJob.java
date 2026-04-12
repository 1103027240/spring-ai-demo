package cn.example.flink.demo.job.transformation;

import cn.example.flink.demo.job.init.UserVisitorBuilder;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.Objects;

/**
 * 1、用户访问量统计
 */
public class UserVisitorJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.fromCollection(UserVisitorBuilder.buildUserVisitorDto())
                .filter(e -> !Objects.equals("3", e.getUserId()))
                .map(e -> new Tuple2<>(e.getUserId(), 1L))
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                // 统计每个用户访问量
                .keyBy(e -> e.f0)
                .sum(1)
                // 查询访问量最多的是哪个用户
                .keyBy(e -> e.f0)
                .maxBy(1)
                .print("maxResult");
        
        env.execute();
    }

}
