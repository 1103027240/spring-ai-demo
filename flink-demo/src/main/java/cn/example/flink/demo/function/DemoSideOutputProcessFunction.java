package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class DemoSideOutputProcessFunction extends ProcessFunction<UserVisitorDto, String> {

    private OutputTag<Tuple3<String, String, Long>> user1OutputTag;
    private OutputTag<Tuple3<String, String, Long>> user2OutputTag;

    public DemoSideOutputProcessFunction(OutputTag<Tuple3<String, String, Long>> user1OutputTag,
                                         OutputTag<Tuple3<String, String, Long>> user2OutputTag) {
        this.user1OutputTag = user1OutputTag;
        this.user2OutputTag = user2OutputTag;
    }

    @Override
    public void processElement(UserVisitorDto value, Context ctx, Collector<String> out) throws Exception {
        if ("1".equals(value.getUserId())) {
            ctx.output(user1OutputTag, Tuple3.of(value.getUserId(), value.getUrl(), value.getTimestamp()));
        } else if ("2".equals(value.getUserId())) {
            ctx.output(user2OutputTag, Tuple3.of(value.getUserId(), value.getUrl(), value.getTimestamp()));
        } else {
            out.collect(value.toString());
        }
    }

}
