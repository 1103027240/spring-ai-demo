package cn.example.base.demo.build;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@Slf4j
public class WorkflowBuild {

    public static String generateExecutionId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis();
    }

    public static String generateWorkflowId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
