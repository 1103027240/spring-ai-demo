package cn.example.flink.demo.function;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

@Slf4j
public class ParallelNumberSourceFunction extends RichParallelSourceFunction<String> {

    private static volatile boolean isRunning = true;

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        int subTaskIndex = getRuntimeContext().getIndexOfThisSubtask();
        log.info("subTaskIndex: {}", subTaskIndex);

        for (int i = 1; i <= 10 && isRunning; i++) {
            if(i % 2 == 1 && subTaskIndex % 2 == 1) {
                ctx.collect(String.format("子任务号[%s]：%s", subTaskIndex, i));
            }

            if(i % 2 == 0 && subTaskIndex % 2 == 0) {
                ctx.collect(String.format("子任务号[%s]：%s", subTaskIndex, i));
            }

            Thread.sleep(100);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

}
