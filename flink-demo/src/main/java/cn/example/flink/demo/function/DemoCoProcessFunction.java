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

    /**
     * 传入元素value，对应分组key
     * appValueState：如果含传入元素分组key，那么appValueState不为空；如果不含传入元素分组key，那么appValueState为null
     * thirdPartyValueState：如果含传入元素分组key，那么thirdPartyValueState不为空；如果不含传入元素分组key，那么thirdPartyValueState为null
     */
    @Override
    public void processElement1(Tuple3<String, String, Long> value, Context ctx, Collector<String> out) throws Exception {
        System.out.println(String.format("app对账请求参数: %s, appValueState: %s, thirdPartyValueState: %s", value, appValueState.value(), thirdPartyValueState.value()));

        // 校验第三方平台是否有该订单
        if (thirdPartyValueState.value() != null) {
            out.collect(String.format("对账成功，app：%s，thirdParty：%s", value, thirdPartyValueState.value()));
            thirdPartyValueState.clear();
        } else {
            appValueState.update(value);
            System.out.println(String.format("app更新后: %s, appValueState: %s, thirdPartyValueState: %s", value, appValueState.value(), thirdPartyValueState.value()));
            ctx.timerService().registerEventTimeTimer(value.f2 + 3000L);
        }
    }

    /**
     * 传入元素value，对应分组key
     * appValueState：如果含传入元素分组key，那么appValueState不为空；如果不含传入元素分组key，那么appValueState为null
     * thirdPartyValueState：如果含传入元素分组key，那么thirdPartyValueState不为空；如果不含传入元素分组key，那么thirdPartyValueState为null
     */
    @Override
    public void processElement2(Tuple4<String, String, String, Long> value, Context ctx, Collector<String> out) throws Exception {
        System.out.println(String.format("thirdParty对账请求参数: %s, appValueState: %s, thirdPartyValueState: %s", value, appValueState.value(), thirdPartyValueState.value()));

        // 校验平台是否有该订单
        if (appValueState.value() != null) {
            out.collect(String.format("对账成功，app：%s，thirdParty：%s", appValueState.value(), value));
            appValueState.clear();
        } else {
            thirdPartyValueState.update(value);
            System.out.println(String.format("thirdParty更新后: %s, appValueState: %s, thirdPartyValueState: %s", value, appValueState.value(), thirdPartyValueState.value()));
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
