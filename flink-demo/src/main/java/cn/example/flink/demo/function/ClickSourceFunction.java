package cn.example.flink.demo.function;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import java.security.SecureRandom;

public class ClickSourceFunction implements SourceFunction<UserVisitorDto> {

    private static volatile boolean isRunning = true;

    @Override
    public void run(SourceContext<UserVisitorDto> ctx) throws Exception {
        SecureRandom random = new SecureRandom();
        String[] userNames = {"1", "2", "3", "4"};
        String[] urls = {"/home", "/api/product", "/api/cart", "/api/order", "/api/pay", "/api/fulfillment"};

        while (isRunning) {
            ctx.collect(new UserVisitorDto(userNames[random.nextInt(userNames.length)], urls[random.nextInt(urls.length)], System.currentTimeMillis()));
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

}
