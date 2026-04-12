package cn.example.flink.demo.job.transformation;

import cn.example.flink.demo.build.UserBuilder;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 2、分区策略
 */
public class PartitionStrategyJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        env.fromCollection(UserBuilder.buildUserDto())
                //.shuffle()
                .rebalance()
                //.rescale()
                //.broadcast()
                .print()
                .setParallelism(4);

        env.execute();
    }

}
