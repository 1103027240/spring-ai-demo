package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum KnowledgeDocumentSourceEnum {

    MANUAL("manual", "手动录入", "系统管理员手动录入"),

    IMPORT("import", "批量导入", "从文件批量导入"),

    API("api", "API接口", "通过API接口创建"),

    CRAWLER("crawler", "网络爬虫", "网络爬虫自动采集"),

    USER_SUBMIT("user_submit", "用户提交", "用户主动提交"),

    SYSTEM("system", "系统生成", "系统自动生成"),

    ;

    private String id;

    private String text;

    private String detailText;

}
