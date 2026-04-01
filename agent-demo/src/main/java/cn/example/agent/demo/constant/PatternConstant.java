package cn.example.agent.demo.constant;

import java.util.regex.Pattern;

public class PatternConstant {

    public static final Pattern MARKDOWN_CODE_BLOCK_PATTERN = Pattern.compile("^(?i)```(?:json)?\\s*|```\\s*$", Pattern.MULTILINE);

}
