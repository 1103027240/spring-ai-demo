package cn.example.flink.demo.job.transformation;

import cn.example.flink.demo.job.init.UserVisitorBuilder;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 3、加盐处理，解决keyBy数据倾斜
 */
public class SaltUserVisitorJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        // 加盐聚合
        SingleOutputStreamOperator<Tuple2<String, Long>> sumAgg = env.fromCollection(UserVisitorBuilder.buildUserVisitorDto())
                .map(e -> {
                    int salt = ThreadLocalRandom.current().nextInt(2);
                    String userId = String.format("%s_%s", e.getUserId(), salt);
                    return new Tuple2<>(userId, 1L);
                })
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(e -> e.f0)
                .sum(1);
        sumAgg.print();

        // 去盐聚合
        sumAgg
                .map(e -> {
                    String userId = e.f0.split("_")[0];
                    return new Tuple2<>(userId, 1L);
                })
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(e -> e.f0)
                .sum(1)
                .print();

        env.execute();
    }

}
