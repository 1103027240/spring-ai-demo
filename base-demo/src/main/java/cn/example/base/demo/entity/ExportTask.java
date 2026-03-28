package cn.example.base.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导出任务实体
 */
@Data
@TableName("export_task")
@Accessors(chain = true)
public class ExportTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 导出格式：xlsx/csv/json/gzip
     */
    @TableField("format")
    private String format;

    /**
     * 导出分类
     */
    @TableField("category")
    private String category;

    /**
     * 导出请求参数（JSON格式）
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 导出字段列表
     */
    @TableField("fields")
    private String fields;

    /**
     * 文件路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小
     */
    @TableField("file_size")
    private Long fileSize = 0L;

    /**
     * 任务状态：INITIALIZED/PROCESSING/COMPLETED/FAILED/CANCELLED
     */
    @TableField("task_status")
    private String taskStatus = "PENDING";

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 耗时（毫秒）
     */
    @TableField("duration")
    private Long duration = 0L;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
