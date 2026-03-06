package cn.getech.base.demo.check;

import cn.getech.base.demo.dto.TextMetricsDto;
import cn.hutool.core.util.StrUtil;

/**
 * Token 文本判断器
 * 用于判断文本是否适合使用 token 分割器
 * @author 11030
 */
public class TokenTextTypeCheck {

    // 常量定义
    private static final int MIN_TEXT_LENGTH = 50;
    private static final int MIN_TOKEN_COUNT = 50;
    private static final int MIN_SPLIT_LENGTH = 100;
    private static final double MIN_ASCII_RATIO = 0.6;
    private static final double MIN_WORD_DENSITY = 0.05;
    private static final double MIN_WHITESPACE_RATIO = 0.05;
    
    /**
     * 判断文本是否适合使用 token 分割器
     * @param text 待判断的文本
     * @return 是否适合使用 token 分割器
     */
    public static boolean isToken(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        
        // 1. 排除明显不适合token分割的文本类型
        if (DetectTextTypeCheck.isChineseText(text) ||
            DetectTextTypeCheck.isMarkdown(text) ||
            DetectTextTypeCheck.isHtml(text) ||
            DetectTextTypeCheck.isCode(text)) {
            return false;
        }
        
        // 2. 检查文本长度
        int textLength = text.length();
        
        // 短文本（少于50个字符）通常不需要token分割
        if (textLength < MIN_TEXT_LENGTH) {
            return false;
        }
        
        // 3. 一次性遍历统计所有指标，提高性能
        TextMetricsDto metrics = calculateTextMetrics(text);
        
        // 4. 估算 token 数量
        int estimatedTokenCount = estimateTokenCount(metrics);
        
        // 5. 综合判断
        // - ASCII字符比例超过60%
        // - 单词密度超过0.05（平均20个字符一个单词）
        // - 空格比例超过0.05（平均20个字符一个空格）
        // - 文本长度超过100个字符（适合token分割的长度）
        // - 估算的 token 数量超过50（适合token分割的数量）
        return metrics.getAsciiRatio() > MIN_ASCII_RATIO &&
               metrics.getWordDensity() > MIN_WORD_DENSITY &&
               metrics.getWhitespaceRatio() > MIN_WHITESPACE_RATIO &&
               textLength > MIN_SPLIT_LENGTH &&
               estimatedTokenCount > MIN_TOKEN_COUNT;
    }
    
    /**
     * 计算文本的 metrics
     * @param text 待计算的文本
     * @return 文本 metrics
     */
    private static TextMetricsDto calculateTextMetrics(String text) {
        int textLength = text.length();
        long wordCount;
        long whitespaceCount = 0;
        long asciiCount = 0;
        long chineseCharCount = 0;
        long digitAndPunctuationCount = 0;
        
        // 一次性遍历统计所有指标
        String[] words = text.split("\\s+");
        wordCount = words.length;
        
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                whitespaceCount++;
            } else if ((c >= 32 && c <= 126) || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                asciiCount++;
            } else if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseCharCount++;
            } else if (Character.isDigit(c) || isPunctuation(c)) {
                digitAndPunctuationCount++;
            }
        }
        
        double wordDensity = textLength > 0 ? (double) wordCount / textLength : 0;
        double whitespaceRatio = textLength > 0 ? (double) whitespaceCount / textLength : 0;
        double asciiRatio = textLength > 0 ? (double) asciiCount / textLength : 0;
        
        return new TextMetricsDto(wordCount, whitespaceCount, asciiCount, chineseCharCount, digitAndPunctuationCount,
                wordDensity, whitespaceRatio, asciiRatio);
    }
    
    /**
     * 估算文本的 token 数量
     * @param metrics 文本 metrics
     * @return 估算的 token 数量
     */
    private static int estimateTokenCount(TextMetricsDto metrics) {
        int tokenCount = 0;
        
        // 英文单词
        tokenCount += metrics.getWordCount();
        
        // 中文字符
        tokenCount += metrics.getChineseCharCount();
        
        // 数字和标点符号
        tokenCount += metrics.getDigitAndPunctuationCount();
        
        // 调整估算结果
        // 实际 token 数量通常会比简单统计少，因为很多标点符号会与单词合并
        return Math.max(1, tokenCount / 2);
    }
    
    /**
     * 判断字符是否为标点符号
     * @param c 字符
     * @return 是否为标点符号
     */
    private static boolean isPunctuation(char c) {
        return c == '.' || c == ',' || c == '!' || c == '?' || c == ';' || c == ':' ||
               c == '"' || c == '\'' || c == '(' || c == ')' || c == '[' || c == ']' ||
               c == '{' || c == '}' || c == '-' || c == '_' || c == '+' || c == '=' ||
               c == '*' || c == '/' || c == '\\' || c == '|' || c == '&' || c == '^' ||
               c == '%' || c == '$' || c == '#' || c == '@' || c == '!' || c == '~' ||
               c == '`' || c == '<' || c == '>' || c == '?' || c == '，' || c == '。' ||
               c == '！' || c == '？' || c == '；' || c == '：' || c == '"' || c == '\'' ||
               c == '（' || c == '）' || c == '[' || c == ']' || c == '{' || c == '}' ||
               c == '、' || c == '|' || c == '…' || c == '—';
    }

}
