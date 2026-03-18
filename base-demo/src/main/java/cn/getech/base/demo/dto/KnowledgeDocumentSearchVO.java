package cn.getech.base.demo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

@Data
@HeadRowHeight(20)
@ContentRowHeight(15)
@Schema(description = "知识库文档导出字段映射")
public class KnowledgeDocumentSearchVO {

    @ExcelProperty(value = "文档ID", index = 0)
    @ColumnWidth(15)
    @Schema(description = "文档ID")
    private Long docId;

    @ExcelProperty(value = "标题", index = 1)
    @ColumnWidth(30)
    @Schema(description = "标题")
    private String title;

    @ExcelProperty(value = "内容", index = 2)
    @ColumnWidth(50)
    @Schema(description = "内容")
    private String content;

    @ExcelProperty(value = "摘要", index = 3)
    @ColumnWidth(40)
    @Schema(description = "摘要")
    private String summary;

    @ExcelProperty(value = "分类ID", index = 4)
    @ColumnWidth(15)
    @Schema(description = "分类ID")
    private Long categoryId;

    @ExcelProperty(value = "关键词", index = 5)
    @ColumnWidth(25)
    @Schema(description = "关键词")
    private String keywords;

    @ExcelProperty(value = "标签", index = 6)
    @ColumnWidth(25)
    @Schema(description = "标签")
    private String tags;

    @ExcelProperty(value = "来源", index = 7)
    @ColumnWidth(20)
    @Schema(description = "来源")
    private String source;

    @ExcelProperty(value = "作者", index = 8)
    @ColumnWidth(20)
    @Schema(description = "作者")
    private String author;

    @ExcelProperty(value = "相似度分数", index = 9)
    @ColumnWidth(15)
    @Schema(description = "相似度分数")
    private Float score;

    @ExcelProperty(value = "创建时间", index = 10)
    @ColumnWidth(20)
    @Schema(description = "创建时间")
    private String createTime;

    // 转换方法
    public static KnowledgeDocumentSearchVO fromKnowledgeDocumentVO(KnowledgeDocumentVO  doc) {
        KnowledgeDocumentSearchVO exportVO = new KnowledgeDocumentSearchVO();

        exportVO.setDocId(doc.getDocId());
        exportVO.setContent(doc.getContent());
        exportVO.setScore(doc.getScore());
        exportVO.setCreateTime(formatTime(doc.getCreateTime()));

        if (doc.getMetadata() != null) {
            Map<String, Object> metadata = doc.getMetadata();
            exportVO.setTitle(getStringValue(metadata, "title"));
            exportVO.setSummary(getStringValue(metadata, "summary"));
            exportVO.setCategoryId(getLongValue(metadata, "categoryId"));
            exportVO.setAuthor(getStringValue(metadata, "author"));
            exportVO.setSource(getStringValue(metadata, "source"));

            Object keywordsObj = doc.getMetadata().get("keywords");
            if (keywordsObj instanceof List) {
                exportVO.setKeywords(String.join(";", (List<String>) keywordsObj));
            } else if (keywordsObj != null) {
                exportVO.setKeywords(keywordsObj.toString());
            }

            Object tagsObj = doc.getMetadata().get("tags");
            if (tagsObj instanceof List) {
                exportVO.setTags(String.join(";", (List<String>) tagsObj));
            } else if (tagsObj != null) {
                exportVO.setTags(tagsObj.toString());
            }
        }

        return exportVO;
    }

    private static String getStringValue(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : "";
    }

    private static Long getLongValue(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String formatTime(Long timestamp) {
        if (timestamp == null) {
            return "";
        }
        return new SimpleDateFormat(NORM_DATETIME_PATTERN).format(new Date(timestamp));
    }

}