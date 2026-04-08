package cn.example.flink.demo.job.source;

import cn.example.flink.demo.job.init.UserBuilder;
import cn.example.flink.demo.job.param.UserDto;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 4、从集合或数组中读取数据
 */
public class CollectionElementsJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<UserDto> dataSourceStream1 = env.fromCollection(UserBuilder.buildUserDto());
        dataSourceStream1.print("collectionData");

        DataStreamSource<UserDto> dataSourceStream2 = env.fromElements(UserBuilder.buildUserDto().toArray(new UserDto[0]));
        dataSourceStream2.print("elementData");

        env.execute();
    }

}
