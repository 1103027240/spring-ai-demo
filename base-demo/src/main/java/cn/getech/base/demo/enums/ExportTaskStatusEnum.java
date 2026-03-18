package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExportTaskStatusEnum {

    INITIALIZED("INITIALIZED", "初始化"),

    PROCESSING("PROCESSING", "处理中"),

    COMPLETED("COMPLETED", "已完成"),

    FAILED("FAILED", "失败"),

    CANCELLED("CANCELLED", "已取消")

    ;

    private String id;

    private String text;

}
