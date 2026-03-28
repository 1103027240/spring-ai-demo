package cn.example.base.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 * @author 11030
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitorAspect {

    /**
     * 慢操作阈值（毫秒）
     */
    private static final long SLOW_OPERATION_THRESHOLD = 1000;
    private static final long VERY_SLOW_OPERATION_THRESHOLD = 3000;

    /**
     * 监控Controller层方法
     */
    @Around("execution(* cn.example.base.demo.controller..*.*(..))")
    public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethod(joinPoint, "Controller");
    }

    /**
     * 监控Service层方法
     */
    @Around("execution(* cn.example.base.demo.service..*.*(..))")
    public Object monitorService(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethod(joinPoint, "Service");
    }

    /**
     * 监控Node层方法（工作流节点）
     */
    @Around("execution(* cn.example.base.demo.node..*.apply(..))")
    public Object monitorNode(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethod(joinPoint, "Node");
    }

    /**
     * 通用方法监控
     */
    private Object monitorMethod(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = String.format("[%s] %s.%s", layer, className, methodName);

        long startTime = System.currentTimeMillis();
        Object result;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 根据执行时间记录不同级别的日志
            if (exception != null) {
                log.error("【性能监控】{} 执行失败，耗时: {}ms，异常: {}", fullMethodName, duration, exception.getMessage());
            } else if (duration > VERY_SLOW_OPERATION_THRESHOLD) {
                log.warn("【性能监控-非常慢】{} 执行时间过长！耗时: {}ms", fullMethodName, duration);
            } else if (duration > SLOW_OPERATION_THRESHOLD) {
                log.warn("【性能监控-慢】{} 执行较慢，耗时: {}ms", fullMethodName, duration);
            } else {
                log.debug("【性能监控】{} 执行完成，耗时: {}ms", fullMethodName, duration);
            }

            // 定期输出性能统计（这里可以扩展为更复杂的统计逻辑）
            if (duration > SLOW_OPERATION_THRESHOLD) {
                logSlowOperationDetails(fullMethodName, duration, joinPoint.getArgs());
            }
        }
    }

    /**
     * 记录慢操作的详细信息
     */
    private void logSlowOperationDetails(String methodName, long duration, Object[] args) {
        StringBuilder details = new StringBuilder();
        details.append("【慢操作详情】方法: ").append(methodName)
                .append(", 耗时: ").append(duration).append("ms");

        if (args != null && args.length > 0) {
            details.append(", 参数: [");
            for (int i = 0; i < Math.min(args.length, 3); i++) {
                if (i > 0) details.append(", ");
                Object arg = args[i];
                String argStr = arg != null ? arg.toString() : "null";
                // 限制参数日志长度
                if (argStr.length() > 100) {
                    argStr = argStr.substring(0, 100) + "...";
                }
                details.append(argStr);
            }
            if (args.length > 3) {
                details.append(", ... (共").append(args.length).append("个参数)");
            }
            details.append("]");
        }

        log.warn(details.toString());

        // 可以在这里添加报警逻辑，例如发送到监控系统
        // sendAlert(methodName, duration);
    }

}
