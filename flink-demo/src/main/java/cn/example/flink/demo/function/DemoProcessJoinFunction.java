package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.util.Collector;
import java.sql.Timestamp;

public class DemoProcessJoinFunction extends ProcessJoinFunction<UserVisitorDto, UserVisitorDto, String> {

    @Override
    public void processElement(UserVisitorDto left, UserVisitorDto right, Context ctx, Collector<String> out) throws Exception {
        UserVisitorDto earlier = left.getTimestamp() <= right.getTimestamp() ? left : right;
        UserVisitorDto later = left.getTimestamp() > right.getTimestamp() ? left : right;

        String result = String.format("%s ===》%s",
                Tuple3.of(earlier.getUserId(), new Timestamp(earlier.getTimestamp()), earlier.getUrl()),
                Tuple3.of(later.getUserId(), new Timestamp(later.getTimestamp()), later.getUrl()));
        out.collect(result);
    }

}
