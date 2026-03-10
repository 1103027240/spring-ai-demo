package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SplitterTypeEnum {

    TOKEN("token", "Token 分割", "基于 Token 数量的分割，适合 LLM 处理", "LLM 处理、API 调用"),

    RECURSIVE("recursive", "递归字符分割", "递归字符分割，按分隔符优先级分割（推荐）", "通用文本、混合内容"),

    SENTENCE("sentence", "句子分割", "句子分割，基于句子边界", "自然语言、文档翻译"),

    CHARACTER("character", "字符分割", "字符分割，按固定字符数分割", "格式化文本、固定宽度内容"),

    PARAGRAPH("paragraph", "段落分割", "段落分割，按空行分割", "长文档、文章"),

    MARKDOWN("markdown", "Markdown 分割", "Markdown 分割，按标题结构分割", "Markdown 文档、技术文档"),

    CHINESE("chinese", "中文分割", "中文优化分割，针对中文标点优化", "中文内容、新闻、小说"),

    HTML("html", "HTML 分割", "HTML 分割，保持标签完整性", "网页内容、HTML 文档"),

    JSON("json", "JSON 分割", "JSON 分割，尝试保持 JSON 结构", "JSON 数据、API 响应"),

    CODE("code", "代码分割", "代码分割，针对代码结构优化", "源代码、配置文件"),

    ;

    private String id;
    private String name;
    private String description;
    private String recommendedUseCases;

    public static boolean checkAlgorithmExist(String algorithm){
        long count = Arrays.asList(SplitterTypeEnum.values()).stream()
                .filter(e -> Objects.equals(e.getId(), algorithm))
                .count();
        return count > 0;
    }

}
