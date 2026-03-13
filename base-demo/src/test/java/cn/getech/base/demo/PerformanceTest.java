package cn.getech.base.demo;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 性能测试脚本
 * 用于测试 /api/base-demo/customerService/chat 接口性能
 * @author 11030
 */
@Slf4j
public class PerformanceTest {

    private static final String BASE_URL = "http://localhost:8030/api/base-demo";
    private static final String CHAT_URL = BASE_URL + "/customerService/chat";

    /**
     * 单次请求测试
     */
    public static void singleRequestTest() {
        log.info("========== 单次请求测试 ==========");

        long startTime = System.currentTimeMillis();

        try {
            HttpResponse response = HttpRequest.post(CHAT_URL)
                    .form("message", "我想查询一下我的订单")
                    .form("userId", 123456L)
                    .form("userName", "测试用户")
                    .timeout(30000)  // 30秒超时
                    .execute();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("请求状态码: {}", response.getStatus());
            log.info("请求耗时: {}ms", duration);
            log.info("响应内容: {}", response.body());

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                log.info("AI回复: {}", result.getStr("aiResponse"));
            }
        } catch (Exception e) {
            log.error("请求失败", e);
        }
    }

    /**
     * 并发请求测试
     */
    public static void concurrentRequestTest(int threadCount, int requestPerThread) {
        log.info("========== 并发请求测试 ==========");
        log.info("线程数: {}, 每个线程请求数: {}, 总请求数: {}",
                threadCount, requestPerThread, threadCount * requestPerThread);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * requestPerThread);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long[] durations = new long[threadCount * requestPerThread];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    int index = successCount.get() + failCount.get();
                    long requestStart = System.currentTimeMillis();

                    try {
                        HttpResponse response = HttpRequest.post(CHAT_URL)
                                .form("message", "测试消息 " + System.currentTimeMillis())
                                .form("userId", 123456L)
                                .form("userName", "测试用户")
                                .timeout(30000)
                                .execute();

                        long requestDuration = System.currentTimeMillis() - requestStart;
                        durations[index] = requestDuration;

                        if (response.isOk()) {
                            successCount.incrementAndGet();
                            log.info("请求成功，耗时: {}ms", requestDuration);
                        } else {
                            failCount.incrementAndGet();
                            log.error("请求失败，状态码: {}", response.getStatus());
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("请求异常", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("测试被中断", e);
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        executor.shutdown();

        // 输出统计结果
        log.info("========== 测试结果 ==========");
        log.info("总耗时: {}ms ({}s)", totalDuration, totalDuration / 1000.0);
        log.info("总请求数: {}", threadCount * requestPerThread);
        log.info("成功请求数: {}", successCount.get());
        log.info("失败请求数: {}", failCount.get());
        log.info("成功率: {}", String.format("%.2f%%",
                successCount.get() * 100.0 / (threadCount * requestPerThread)));
        log.info("平均耗时: {}ms",
                totalDuration / (double) (threadCount * requestPerThread));
        log.info("QPS: {}", String.format("%.2f",
                (threadCount * requestPerThread * 1000.0) / totalDuration));

        // 计算耗时统计
        long minDuration = Long.MAX_VALUE;
        long maxDuration = 0;
        long totalRequestDuration = 0;
        for (long duration : durations) {
            if (duration > 0) {
                minDuration = Math.min(minDuration, duration);
                maxDuration = Math.max(maxDuration, duration);
                totalRequestDuration += duration;
            }
        }

        log.info("最小耗时: {}ms", minDuration);
        log.info("最大耗时: {}ms", maxDuration);
        log.info("平均请求耗时: {}ms",
                totalRequestDuration / (double) successCount.get());

        // 计算P50, P90, P95, P99
        long[] sortedDurations = new long[successCount.get()];
        int idx = 0;
        for (long duration : durations) {
            if (duration > 0) {
                sortedDurations[idx++] = duration;
            }
        }
        java.util.Arrays.sort(sortedDurations);

        log.info("P50: {}ms", sortedDurations[sortedDurations.length / 2]);
        log.info("P90: {}ms", sortedDurations[(int) (sortedDurations.length * 0.9)]);
        log.info("P95: {}ms", sortedDurations[(int) (sortedDurations.length * 0.95)]);
        log.info("P99: {}ms", sortedDurations[(int) (sortedDurations.length * 0.99)]);
    }

    /**
     * 压力测试
     */
    public static void stressTest(int threadCount, int durationSeconds) {
        log.info("========== 压力测试 ==========");
        log.info("线程数: {}, 测试时长: {}秒", threadCount, durationSeconds);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        volatile boolean running = true;

        long startTime = System.currentTimeMillis();

        // 启动定时器，在指定时间后停止测试
        new Thread(() -> {
            try {
                Thread.sleep(durationSeconds * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            running = false;
        }).start();

        // 启动多个线程持续发送请求
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                while (running) {
                    long requestStart = System.currentTimeMillis();

                    try {
                        HttpResponse response = HttpRequest.post(CHAT_URL)
                                .form("message", "压测消息 " + System.currentTimeMillis())
                                .form("userId", 123456L)
                                .form("userName", "测试用户")
                                .timeout(30000)
                                .execute();

                        long requestDuration = System.currentTimeMillis() - requestStart;

                        if (response.isOk()) {
                            successCount.incrementAndGet();
                            if (requestDuration > 5000) {
                                log.warn("慢请求: {}ms", requestDuration);
                            }
                        } else {
                            failCount.incrementAndGet();
                            log.error("请求失败，状态码: {}", response.getStatus());
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("请求异常", e);
                    }
                }
            });
        }

        try {
            Thread.sleep(durationSeconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        executor.shutdown();

        // 输出统计结果
        log.info("========== 压力测试结果 ==========");
        log.info("测试时长: {}秒", totalDuration / 1000.0);
        log.info("总请求数: {}", successCount.get() + failCount.get());
        log.info("成功请求数: {}", successCount.get());
        log.info("失败请求数: {}", failCount.get());
        log.info("成功率: {}", String.format("%.2f%%",
                successCount.get() * 100.0 / (successCount.get() + failCount.get())));
        log.info("QPS: {}", String.format("%.2f",
                successCount.get() * 1000.0 / totalDuration));
    }

    public static void main(String[] args) {
        // 单次请求测试
        singleRequestTest();

        System.out.println();

        // 并发请求测试（10个线程，每个线程发送5个请求）
        concurrentRequestTest(10, 5);

        System.out.println();

        // 压力测试（20个线程，持续30秒）
        // stressTest(20, 30);
    }
}
