package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.Objects;

/**
 * 工作流执行状态
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum WorkflowExecutionStatusEnum {

    RUNNING("RUNNING", "执行中"),

    SUCCESS("SUCCESS", "执行成功"),

    FAILED("FAILED", "执行失败"),

    TIMEOUT("TIMEOUT", "执行超时"),

    CANCELLED("CANCELLED" , "已取消"),

    ;

    private String id;

    private String text;

    public static String getText(String id) {
        return Arrays.asList(WorkflowExecutionStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(WorkflowExecutionStatusEnum::getText)
                .orElse("未知");
    }

}
