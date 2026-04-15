package cn.example.flink.demo.job.sink;

import cn.example.flink.demo.function.ClickSourceFunction;
import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

/**
 * 3、输出到Redis
 */
public class RedisSinkJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        FlinkJedisPoolConfig config = new FlinkJedisPoolConfig.Builder()
                .setHost("localhost")
                .setPort(6379)
                .build();

        env.addSource(new ClickSourceFunction(1000L)).addSink(new RedisSink<>(config, new CustomerRedisMapper()));
        env.execute();
    }

    public static class CustomerRedisMapper implements RedisMapper<UserVisitorDto> {

        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.SET);
        }

        @Override
        public String getKeyFromData(UserVisitorDto data) {
            return "user:visitor:" + data.getUserId();
        }

        @Override
        public String getValueFromData(UserVisitorDto data) {
            return data.getUrl();
        }
    }

}
