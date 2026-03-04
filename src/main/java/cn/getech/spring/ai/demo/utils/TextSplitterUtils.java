package cn.getech.spring.ai.demo.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本分割工具类
 * @author 11030
 */
@Slf4j
public class TextSplitterUtils {

    private static final int POOL_INITIAL_SIZE = 50;
    private static final int POOL_MAX_SIZE = 100;
    private static final int STRING_BUILDER_CAPACITY = 1024;

    /** StringBuilder 对象池 */
    private static final ConcurrentLinkedDeque<StringBuilder> POOL = new ConcurrentLinkedDeque<>();

    static {
        initStringBuilderPool();
    }

    /**
     * 初始化 StringBuilder 对象池
     */
    private static void initStringBuilderPool() {
        for (int i = 0; i < POOL_INITIAL_SIZE; i++) {
            POOL.offer(new StringBuilder(STRING_BUILDER_CAPACITY));
        }
    }

    public static List<String> splitParagraph(String text, Pattern pattern, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        // 预处理：统一换行符
        text = text.replaceAll("\\r\\n", "\n");

        // 按段落边界分割
        List<String> sections = Arrays.stream(pattern.split(text))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = acquireStringBuilder();

        try {
            for (String section : sections) {
                // 检查当前块大小
                if (currentChunk.length() + section.length() > chunkSize) {
                    if (currentChunk.length() > 0) {
                        // 移除末尾可能的多余换行符
                        String chunkText = currentChunk.toString().replaceAll("\\n+$", "\n");
                        chunks.add(chunkText);
                        currentChunk.setLength(0);
                    }
                }
                // 添加当前部分，保持段落格式
                currentChunk.append(section).append("\n\n");
            }

            // 添加最后一个块
            if (currentChunk.length() > 0) {
                // 移除末尾可能的多余换行符
                String chunkText = currentChunk.toString().replaceAll("\\n+$", "\n");
                chunks.add(chunkText);
            }

            return applyOverlap(chunks, chunkOverlap);
        } finally {
            releaseStringBuilder(currentChunk);
        }
    }

    /**
     * 按字符计数分割（在标点处断开，尽量保持单词完整）
     * @param text 待分割文本
     * @param chunkSize 块大小
     * @param chunkOverlap 块重叠量
     * @param preserveWords 是否保持单词完整
     * @return 分割后的文本块列表
     */
    public static List<String> splitByCharacterCount(String text, int chunkSize, int chunkOverlap, boolean preserveWords) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        int end;

        // 定义更多的单词边界字符
        char[] wordBoundaries = {' ', '.', ',', ';', '!', '?', '。', '，', '；', '！', '？'};

        while (start < text.length()) {
            end = Math.min(start + chunkSize, text.length());

            // 如果需要保持单词完整，并且不是文本末尾
            if (preserveWords && end < text.length()) {
                // 查找最近的单词边界
                int latestBoundary = -1;
                for (char boundary : wordBoundaries) {
                    int pos = text.lastIndexOf(boundary, end);
                    if (pos > latestBoundary) {
                        latestBoundary = pos;
                    }
                }

                // 选择最近的边界
                if (latestBoundary > start + chunkSize * 0.8) { // 确保不会分割过小
                    end = latestBoundary + 1;
                }
            }

            chunks.add(text.substring(start, end));
            start = end - chunkOverlap;
        }

        return chunks;
    }

    /**
     * HTML 分割（保持结构完整）
     * @param text 待分割 HTML 文本
     * @param pattern 分割正则
     * @param chunkSize 块大小
     * @param chunkOverlap 块重叠量
     * @return HTML 块列表
     */
    public static List<String> splitHtml(String text, Pattern pattern, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = acquireStringBuilder();
        int tagDepth = 0; // 跟踪HTML标签深度

        try {
            // 分割HTML标签和文本
            String[] parts = pattern.split(text);
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                int partLength = part.length();

                // 检查是否需要分割，只有在标签深度为0时才分割
                if (currentChunk.length() + partLength > chunkSize && tagDepth == 0) {
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                    }
                }

                // 添加当前部分
                currentChunk.append(part);

                // 处理标签，更新标签深度
                if (i < parts.length - 1) {
                    String tag = text.substring(
                            text.indexOf(part) + part.length(),
                            text.indexOf(parts[i + 1])
                    );
                    currentChunk.append(tag);

                    // 更新标签深度
                    tagDepth += countOpenTags(tag) - countCloseTags(tag);
                }
            }

            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

            return applyOverlap(chunks, chunkOverlap);
        } finally {
            releaseStringBuilder(currentChunk);
        }
    }

    /**
     * Markdown 分割（没有保持结构完整）
     * @param text 待分割 Markdown 文本
     * @param pattern 分割正则
     * @param chunkSize 块大小
     * @param chunkOverlap 块重叠量
     * @return Markdown 块列表
     */
    public static List<String> splitMarkdown(String text, Pattern pattern, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        // 按标题分割
        List<String> sections = Arrays.stream(pattern.split(text))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = acquireStringBuilder();

        try {
            for (String section : sections) {
                // 检查当前块大小
                if (currentChunk.length() + section.length() > chunkSize) {
                    if (currentChunk.length() > 0) {
                        // 移除末尾可能的多余换行符
                        String chunkText = currentChunk.toString().replaceAll("\\n+$", "\n");
                        chunks.add(chunkText);
                        currentChunk.setLength(0);
                    }
                }
                // 添加当前部分，保持Markdown格式
                currentChunk.append(section).append("\n\n");
            }

            // 添加最后一个块
            if (currentChunk.length() > 0) {
                // 移除末尾可能的多余换行符
                String chunkText = currentChunk.toString().replaceAll("\\n+$", "\n");
                chunks.add(chunkText);
            }

            return applyOverlap(chunks, chunkOverlap);
        } finally {
            releaseStringBuilder(currentChunk);
        }
    }

    /**
     * HTML 分割（没有保持结构完整）
     * @param text 待分割 HTML 文本
     * @param pattern 分割正则
     * @param chunkSize 块大小
     * @param chunkOverlap 块重叠量
     * @return HTML 块列表
     */
    public static List<String> splitHtmlWithNoStructure(String text, Pattern pattern, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        // 预处理：移除多余空白，保持HTML结构
        text = text.replaceAll("\\s+", " ").trim();

        // 按标签分割
        List<String> sections = Arrays.stream(pattern.split(text))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = acquireStringBuilder();

        try {
            for (String section : sections) {
                // 检查当前块大小
                if (currentChunk.length() + section.length() > chunkSize) {
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                    }
                }
                // 添加当前部分，保持HTML格式
                currentChunk.append(section).append(" ");
            }

            // 添加最后一个块
            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

            return applyOverlap(chunks, chunkOverlap);
        } finally {
            releaseStringBuilder(currentChunk);
        }
    }

    /**
     * JSON 分割（保持结构完整）
     * @param text 待分割 JSON 文本
     * @param pattern 分割正则
     * @return JSON 块列表
     */
    public static List<String> splitJson(String text, Pattern pattern) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        // 验证JSON格式
        if (!JSONUtil.isTypeJSON(text)) {
            return List.of(text);
        }

        // 按对象和数组边界分割
        List<String> sections = Arrays.stream(pattern.split(text))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        return sections;
    }

    /**
     * JSON 分割（保持结构完整）
     * @param text 待分割 JSON 文本
     * @param pattern 分割正则
     * @param chunkSize 块大小
     * @param chunkOverlap 块重叠量
     * @return JSON 块列表
     */
    public static List<String> splitJsonWithChunk(String text, Pattern pattern, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        // 验证JSON格式
        if (!JSONUtil.isTypeJSON(text)) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = acquireStringBuilder();
        int currentLength = 0;

        try {
            // 按对象和数组边界分割
            for (String part : pattern.split(text)) {
                int partLength = part.length();

                if (currentLength + partLength <= chunkSize) {
                    currentChunk.append(part);
                    currentLength += partLength;
                } else {
                    if (currentLength > 0) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                        currentLength = 0;
                    }

                    if (partLength > chunkSize) {
                        // 对大块JSON进行进一步分割
                        chunks.addAll(splitByCharacterCount(part, chunkSize, chunkOverlap, true));
                    } else {
                        currentChunk.append(part);
                        currentLength = partLength;
                    }
                }
            }

            if (currentLength > 0) {
                chunks.add(currentChunk.toString());
            }

            // 应用重叠策略
            return applyOverlap(chunks, chunkOverlap);
        } finally {
            releaseStringBuilder(currentChunk);
        }
    }

    /**
     * 中文文本分割（针对中文特性优化）
     * @param text 待分割中文文本
     * @param pattern 分割正则
     * @param chunkSize 块大小
     * @param chunkOverlap 块重叠量
     * @return 中文文本块列表
     */
    public static List<String> splitChinese(String text, Pattern pattern, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        // 按中文标点分割
        List<String> sections = Arrays.stream(pattern.split(text))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = acquireStringBuilder();

        try {
            for (String section : sections) {
                // 检查当前块大小
                if (currentChunk.length() + section.length() > chunkSize) {
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                    }
                }
                // 添加当前部分，保持中文文本格式
                if (currentChunk.length() > 0 && !currentChunk.toString().endsWith(" ")) {
                    currentChunk.append(" ");
                }
                currentChunk.append(section);
            }

            // 添加最后一个块
            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

            return applyOverlap(chunks, chunkOverlap);
        } finally {
            releaseStringBuilder(currentChunk);
        }
    }

    /**
     * 代码分割（针对代码文件优化）
     * @param text 待分割代码文本
     * @param pattern 分割正则
     * @param chunkSize 块大小
     * @param chunkOverlap 块重叠量
     * @return 代码块列表
     */
    public static List<String> splitCode(String text, Pattern pattern, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        // 按代码结构边界分割
        List<String> sections = Arrays.stream(pattern.split(text))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = acquireStringBuilder();

        try {
            for (String section : sections) {
                // 检查当前块大小
                if (currentChunk.length() + section.length() > chunkSize) {
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                    }
                }
                // 添加当前部分，保持原始代码格式
                currentChunk.append(section);
            }

            // 添加最后一个块
            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

            return applyOverlap(chunks, chunkOverlap);
        } finally {
            releaseStringBuilder(currentChunk);
        }
    }

    /**
     * 应用重叠策略（修正：正确处理重叠部分）
     */
    private static List<String> applyOverlap(List<String> chunks, int overlap) {
        List<String> overlappedChunks = new ArrayList<>(chunks.size());
        if (overlap <= 0 || chunks.size() <= 1) {
            return overlappedChunks;
        }

        // 第一块保持不变
        overlappedChunks.add(chunks.get(0));

        // 从第二块开始，添加前一块的末尾作为重叠
        for (int i = 1; i < chunks.size(); i++) {
            String prev = chunks.get(i - 1);
            String curr = chunks.get(i);

            // 如果前一块足够长，取其末尾作为重叠部分
            if (prev.length() >= overlap) {
                String overlapPart = prev.substring(prev.length() - overlap);
                overlappedChunks.add(overlapPart + curr);
            } else {
                // 前一块太短，直接拼接
                overlappedChunks.add(prev + curr);
            }
        }
        return overlappedChunks;
    }

    /**
     * 计算开标签数量
     */
    private static int countOpenTags(String tag) {
        return (int) tag.chars()
                .filter(c -> c == '<')
                .filter(c -> {
                    int index = tag.indexOf(c);
                    return index + 1 < tag.length() && tag.charAt(index + 1) != '/' && tag.charAt(index + 1) != '!';
                })
                .count();
    }

    /**
     * 计算闭标签数量
     */
    private static int countCloseTags(String tag) {
        return (int) tag.chars()
                .filter(c -> c == '<')
                .filter(c -> {
                    int index = tag.indexOf(c);
                    return index + 1 < tag.length() && tag.charAt(index + 1) == '/';
                })
                .count();
    }

    /**
     * 从对象池获取 StringBuilder
     */
    private static StringBuilder acquireStringBuilder() {
        StringBuilder sb = POOL.pollFirst();
        return sb != null ? sb : new StringBuilder(STRING_BUILDER_CAPACITY);
    }

    /**
     * 归还 StringBuilder 到对象池
     */
    private static void releaseStringBuilder(StringBuilder sb) {
        if (sb != null) {
            sb.setLength(0);
            if (POOL.size() < POOL_MAX_SIZE) {
                POOL.offerLast(sb);
            }
        }
    }

}
