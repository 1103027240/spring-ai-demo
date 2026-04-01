package cn.example.agent.demo.function;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import java.util.function.BiFunction;

@Slf4j
public class SqlExecuteFunction implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(String message, ToolContext toolContext) {
        log.info("toolContext: {}", toolContext.getContext());
        return String.format("用户[%s]执行了一条sql：[%s]", toolContext.getContext(), message);
    }

}
