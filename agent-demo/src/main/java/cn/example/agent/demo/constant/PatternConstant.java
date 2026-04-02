package cn.example.agent.demo.constant;

import java.util.regex.Pattern;

public class PatternConstant {

    public static final Pattern MARKDOWN_CODE_BLOCK_PATTERN = Pattern.compile("^(?i)```(?:json)?\\s*|```\\s*$", Pattern.MULTILINE);

    // SQL注入检测模式
    public static final Pattern SQL_INJECTION_PATTERN =
            Pattern.compile("(?i)(\\b(INSERT\\s+INTO|UPDATE\\s+\\w+|DELETE\\s+FROM|DROP\\s+TABLE|TRUNCATE\\s+TABLE|ALTER\\s+TABLE|CREATE\\s+TABLE)\\b)");

}
