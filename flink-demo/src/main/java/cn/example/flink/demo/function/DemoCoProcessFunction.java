package cn.example.flink.demo.function;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

public class DemoCoProcessFunction extends CoProcessFunction<Tuple3<String, String, Long>, Tuple4<String, String, String, Long>, String> {

    private ValueState<Tuple3<String, String, Long>> appValueState;

    private ValueState<Tuple4<String, String, String, Long>> thirdPartyValueState;

    @Override
    public void open(Configuration parameters) throws Exception {
        appValueState = getRuntimeContext().getState(new ValueStateDescriptor<>("appValueState", Types.TUPLE(Types.STRING, Types.STRING, Types.LONG)));
        thirdPartyValueState = getRuntimeContext().getState(new ValueStateDescriptor<>("thirdPartyValueState", Types.TUPLE(Types.STRING, Types.STRING, Types.STRING, Types.LONG)));
    }

    @Override
    public void processElement1(Tuple3<String, String, Long> value, Context ctx, Collector<String> out) throws Exception {
        // 校验第三方平台是否有该订单
        if (thirdPartyValueState.value() != null) {
            out.collect(String.format("对账成功，app：%s，thirdParty：%s", value, thirdPartyValueState.value()));
            thirdPartyValueState.clear();
        } else {
            appValueState.update(value);
            ctx.timerService().registerEventTimeTimer(value.f2 + 3000L);
        }
    }

    @Override
    public void processElement2(Tuple4<String, String, String, Long> value, Context ctx, Collector<String> out) throws Exception {
        // 校验平台是否有该订单
        if (appValueState.value() != null) {
            out.collect(String.format("对账成功，app：%s，thirdParty：%s", appValueState.value(), value));
            appValueState.clear();
        } else {
            thirdPartyValueState.update(value);
            ctx.timerService().registerEventTimeTimer(value.f3 + 3000L);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
        if (appValueState.value() != null) {
            out.collect(String.format("对账失败，app：%s，thirdParty：%s", appValueState.value(), thirdPartyValueState.value()));
            appValueState.clear();
        }

        if (thirdPartyValueState.value() != null) {
            out.collect(String.format("对账失败，app：%s，thirdParty：%s", appValueState.value(), thirdPartyValueState.value()));
            thirdPartyValueState.clear();
        }
    }

}
