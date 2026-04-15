package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import java.security.SecureRandom;
import java.util.Calendar;

public class ClickV2SourceFunction implements SourceFunction<UserVisitorDto> {

    private Long sleepTime;
    private volatile boolean isRunning = true;

    public ClickV2SourceFunction (Long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public void run(SourceContext<UserVisitorDto> ctx) throws Exception {
        SecureRandom random = new SecureRandom();
        String[] userIds = {"1", "2", "3", "4"};
        String[] urls = {"/home", "/api/product", "/api/cart", "/api/order", "/api/pay", "/api/fulfillment", "/api/stock"};

        while (isRunning) {
            Thread.sleep(sleepTime);
            UserVisitorDto dto = new UserVisitorDto(userIds[random.nextInt(userIds.length)], urls[random.nextInt(urls.length)], Calendar.getInstance().getTimeInMillis());
            ctx.collect(dto);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

}
