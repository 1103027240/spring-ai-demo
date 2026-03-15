//package cn.getech.base.demo.entity;
//
//
//import com.baomidou.mybatisplus.annotation.*;
//import com.example.aicustomer.dto.ExportRequest;
//import com.example.aicustomer.dto.ExportResult;
//import com.fasterxml.jackson.annotation.JsonFormat;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.experimental.Accessors;
//import lombok.extern.slf4j.Slf4j;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
///**
// * 导出任务实体
// */
//@Data
//@EqualsAndHashCode(callSuper = false)
//@Accessors(chain = true)
//@TableName("export_task")
//@Slf4j
//public class ExportTask implements Serializable {
//    private static final long serialVersionUID = 1L;
//
//    @TableId(value = "id", type = IdType.AUTO)
//    private Long id;
//
//    /**
//     * 任务ID
//     */
//    @TableField("task_id")
//    private String taskId;
//
//    /**
//     * 用户ID
//     */
//    @TableField("user_id")
//    private String userId;
//
//    /**
//     * 用户名
//     */
//    @TableField("user_name")
//    private String userName;
//
//    /**
//     * 导出格式：json/csv/xml
//     */
//    @TableField("format")
//    private String format;
//
//    /**
//     * 导出的分类
//     */
//    @TableField("category")
//    private String category;
//
//    /**
//     * 关键词筛选
//     */
//    @TableField("keyword")
//    private String keyword;
//
//    /**
//     * 状态筛选
//     */
//    @TableField("status")
//    private String status;
//
//    /**
//     * 作者筛选
//     */
//    @TableField("author")
//    private String author;
//
//    /**
//     * 是否包含已删除的
//     */
//    @TableField("include_deleted")
//    private Boolean includeDeleted = false;
//
//    /**
//     * 导出字段列表
//     */
//    @TableField("fields")
//    private String fields;
//
//    /**
//     * 导出编码
//     */
//    @TableField("encoding")
//    private String encoding = "UTF-8";
//
//    /**
//     * 文件路径
//     */
//    @TableField("file_path")
//    private String filePath;
//
//    /**
//     * 文件名
//     */
//    @TableField("file_name")
//    private String fileName;
//
//    /**
//     * 文件大小
//     */
//    @TableField("file_size")
//    private Long fileSize = 0L;
//
//    /**
//     * 记录数量
//     */
//    @TableField("record_count")
//    private Integer recordCount = 0;
//
//    /**
//     * 导出请求参数（JSON格式）
//     */
//    @TableField("request_params")
//    private String requestParams;
//
//    /**
//     * 导出结果（JSON格式）
//     */
//    @TableField("result_data")
//    private String resultData;
//
//    /**
//     * 任务状态：PENDING/PROCESSING/COMPLETED/FAILED/CANCELLED
//     */
//    @TableField("task_status")
//    private String taskStatus = "PENDING";
//
//    /**
//     * 错误信息
//     */
//    @TableField("error_message")
//    private String errorMessage;
//
//    /**
//     * 进度百分比
//     */
//    @TableField("progress")
//    private Integer progress = 0;
//
//    /**
//     * 当前阶段
//     */
//    @TableField("current_phase")
//    private String currentPhase = "初始化";
//
//    /**
//     * 开始时间
//     */
//    @TableField("start_time")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime startTime;
//
//    /**
//     * 结束时间
//     */
//    @TableField("end_time")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime endTime;
//
//    /**
//     * 耗时（毫秒）
//     */
//    @TableField("duration")
//    private Long duration = 0L;
//
//    /**
//     * 创建时间
//     */
//    @TableField(value = "create_time", fill = FieldFill.INSERT)
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime createTime;
//
//    /**
//     * 更新时间
//     */
//    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime updateTime;
//
//    /**
//     * 导出请求对象
//     */
//    @TableField(exist = false)
//    private ExportRequest request;
//
//    /**
//     * 导出结果对象
//     */
//    @TableField(exist = false)
//    private ExportResult result;
//
//    /**
//     * 从JSON获取导出请求
//     */
//    public ExportRequest getRequest() {
//        if (request != null) {
//            return request;
//        }
//
//        if (requestParams != null && !requestParams.isEmpty()) {
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                request = mapper.readValue(requestParams, ExportRequest.class);
//            } catch (Exception e) {
//                log.error("解析导出请求参数失败", e);
//            }
//        }
//
//        return request;
//    }
//
//    /**
//     * 设置导出请求
//     */
//    public void setRequest(ExportRequest request) {
//        this.request = request;
//        if (request != null) {
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                this.requestParams = mapper.writeValueAsString(request);
//            } catch (Exception e) {
//                log.error("序列化导出请求参数失败", e);
//            }
//        } else {
//            this.requestParams = null;
//        }
//    }
//
//    /**
//     * 从JSON获取导出结果
//     */
//    public ExportResult getResult() {
//        if (result != null) {
//            return result;
//        }
//
//        if (resultData != null && !resultData.isEmpty()) {
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                result = mapper.readValue(resultData, ExportResult.class);
//            } catch (Exception e) {
//                log.error("解析导出结果失败", e);
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * 设置导出结果
//     */
//    public void setResult(ExportResult result) {
//        this.result = result;
//        if (result != null) {
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                this.resultData = mapper.writeValueAsString(result);
//            } catch (Exception e) {
//                log.error("序列化导出结果失败", e);
//            }
//        } else {
//            this.resultData = null;
//        }
//    }
//
//    /**
//     * 计算进度百分比
//     */
//    public Integer getProgress() {
//        if (progress == null) {
//            return 0;
//        }
//        return progress;
//    }
//
//    /**
//     * 计算耗时
//     */
//    public Long getDuration() {
//        if (startTime != null && endTime != null) {
//            return java.time.Duration.between(startTime, endTime).toMillis();
//        }
//        return duration;
//    }
//}
