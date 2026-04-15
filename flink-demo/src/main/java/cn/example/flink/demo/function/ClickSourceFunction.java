package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ClickSourceFunction implements SourceFunction<UserVisitorDto> {

    private Long sleepTime;
    private volatile boolean isRunning = true;

    public ClickSourceFunction (Long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public void run(SourceContext<UserVisitorDto> ctx) throws Exception {
        SecureRandom random = new SecureRandom();
        String[] userIds = {"1", "2", "3", "4"};
        String[] urls = {"/home", "/api/product", "/api/cart", "/api/order", "/api/pay", "/api/fulfillment", "/api/stock"};
        List<Long> timestamps = LongStream.rangeClosed(1000, 30000).boxed().collect(Collectors.toList());

        while (isRunning) {
            Thread.sleep(sleepTime);
            UserVisitorDto dto = new UserVisitorDto(userIds[random.nextInt(userIds.length)], urls[random.nextInt(urls.length)], timestamps.get(random.nextInt(timestamps.size())));
            ctx.collect(dto);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

}
