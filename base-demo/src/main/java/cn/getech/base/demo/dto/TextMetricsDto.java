package cn.getech.base.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文本度量指标 DTO
 * 用于统计和分析文本的各种特征指标，支持智能分割策略的选择
 * @author 11030
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextMetricsDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 单词数量（英文）或词语数量（中文）
     * 英文按空格分词统计，中文按字符统计
     */
    private long wordCount;

    /**
     * 空白字符数量
     * 包括空格、制表符、换行符等
     */
    private long whitespaceCount;

    /**
     * ASCII 字符数量
     * 包含英文字母、数字、标点符号等
     */
    private long asciiCount;

    /**
     * 中文字符数量
     * 统计 Unicode 编码在 [\u4e00-\u9fa5] 范围内的汉字字符
     */
    private long chineseCharCount;

    /**
     * 数字和标点符号数量
     * 包含阿拉伯数字和各种标点符号
     */
    private long digitAndPunctuationCount;

    /**
     * 单词密度
     * 计算公式：wordCount / textLength，表示单位长度内的单词数
     * 用于判断文本的语言特性（英文/中文）
     */
    private double wordDensity;

    /**
     * 空白字符比例
     * 计算公式：whitespaceCount / textLength
     * 用于评估文本的格式化程度
     */
    private double whitespaceRatio;

    /**
     * ASCII 字符比例
     * 计算公式：asciiCount / textLength
     * 用于判断文本是否以英文为主
     */
    private double asciiRatio;

}
