package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DemoCoGroupFunction implements CoGroupFunction<UserVisitorDto, UserVisitorDto, String> {

    @Override
    public void coGroup(Iterable<UserVisitorDto> iterable1, Iterable<UserVisitorDto> iterable2, Collector<String> out) throws Exception {
        List<Tuple3<String, String, Timestamp>> resultList1 = new ArrayList<>();
        List<Tuple3<String, String, Timestamp>> resultList2 = new ArrayList<>();
        iterable1.forEach(e -> resultList1.add(Tuple3.of(e.getUserId(), e.getUrl(), new Timestamp(e.getTimestamp()))));
        iterable2.forEach(e -> resultList2.add(Tuple3.of(e.getUserId(), e.getUrl(), new Timestamp(e.getTimestamp()))));
        out.collect(String.format("%s ===> %s", resultList1, resultList2));
    }

}
