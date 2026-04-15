package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import java.sql.Timestamp;

public class DemoJoinFunction implements JoinFunction<UserVisitorDto, UserVisitorDto, String> {

    @Override
    public String join(UserVisitorDto dto1, UserVisitorDto dto2) throws Exception {
        Tuple3<String, String, Timestamp> result1 = Tuple3.of(dto1.getUserId(), dto1.getUrl(), new Timestamp(dto1.getTimestamp()));
        Tuple3<String, String, Timestamp> result2 = Tuple3.of(dto2.getUserId(), dto2.getUrl(), new Timestamp(dto2.getTimestamp()));
        return String.format("%s ===> %s", result1, result2);
    }

}
