package cn.example.base.demo.service.impl;

import cn.example.base.demo.param.dto.KnowledgeDocumentExportDto;
import cn.example.base.demo.entity.ExportTask;
import cn.example.base.demo.mapper.ExportTaskMapper;
import cn.example.base.demo.service.ExportTaskService;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static cn.example.base.demo.constant.RedisKeyConstant.EXPORT_FILE_PREFIX;
import static cn.example.base.demo.constant.RedisKeyConstant.EXPORT_TASK_PREFIX;
import static cn.example.base.demo.enums.ExportTaskStatusEnum.INITIALIZED;
import static cn.example.base.demo.enums.ExportTaskStatusEnum.PROCESSING;
import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;

@Slf4j
@Service
public class ExportTaskServiceImpl extends ServiceImpl<ExportTaskMapper, ExportTask> implements ExportTaskService {

    @Value("${export.task.temp-path}")
    private String tempPath;

    @Value("${export.task.ttl-days:7}")
    private int taskTtlDays;

    @Value("${export.task.ttl-minutes:30}")
    private int taskTtlMinutes;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public ExportTask createExportTask(String taskId, KnowledgeDocumentExportDto dto) {
        ExportTask exportTask = new ExportTask();

        // 基本信息
        exportTask.setTaskId(taskId);
        exportTask.setFormat(dto.getFormat());
        exportTask.setTaskStatus(INITIALIZED.getId());
        exportTask.setCreateTime(LocalDateTime.now());
        exportTask.setUpdateTime(LocalDateTime.now());
        exportTask.setUserId("10001"); // 用户ID

        // 导出请求参数
        try {
            exportTask.setRequestParams(objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            log.warn("序列化导出请求参数失败", e);
        }

        // 导出字段列表
        if (dto.getExportFields() != null) {
            try {
                exportTask.setFields(objectMapper.writeValueAsString(dto.getExportFields()));
            } catch (Exception e) {
                log.warn("序列化导出字段列表失败", e);
            }
        }

        // 保存任务
        save(exportTask);

        // 缓存
        String statusKey = EXPORT_TASK_PREFIX + taskId;
        redisTemplate.opsForValue().set(statusKey, JSONObject.toJSONString(exportTask), taskTtlMinutes, TimeUnit.HOURS);
        return exportTask;
    }

    @Override
    public void updateTaskStatus(ExportTask exportTask, String status, String fileName, Path filePath, String errorMessage) {
        exportTask.setTaskStatus(status);
        exportTask.setUpdateTime(LocalDateTime.now());

        if(StrUtil.isNotBlank(fileName)){
            exportTask.setFileName(fileName);
        }

        if(filePath != null){
            exportTask.setFilePath(filePath.toFile().getPath());
            try {
                long fileSize = Files.size(filePath);
                exportTask.setFileSize(fileSize);
            } catch (IOException e) {
                log.warn("获取文件大小失败: filePath={}", filePath, e);
            }
        }

        if(StrUtil.isNotBlank(errorMessage)){
            exportTask.setErrorMessage(errorMessage);
        }

        // 更新
        updateById(exportTask);

        // 缓存处理
        if(Arrays.asList(INITIALIZED.getId(), PROCESSING.getId()).contains(exportTask.getTaskStatus())){
            String statusKey = EXPORT_TASK_PREFIX + exportTask.getTaskId();
            redisTemplate.opsForValue().set(statusKey, JSONObject.toJSONString(exportTask), taskTtlMinutes, TimeUnit.HOURS);
        } else {
            redisTemplate.delete(EXPORT_TASK_PREFIX + exportTask.getTaskId());
        }
    }

    @Override
    public void cleanupTaskTempFiles(String taskId) throws IOException {
        // 查找任务记录
        ExportTask task = getById(taskId);
        if (task != null) {
            // 清理文件
            if (StrUtil.isNotBlank(task.getFilePath())) {
                Path filePath = Paths.get(task.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("已删除任务文件: taskId={}, file={}", taskId, task.getFilePath());
                }
            }

            // 清理临时目录
            Path tempDir = Paths.get(tempPath, taskId);
            if (Files.exists(tempDir)) {
                FileUtils.deleteDirectory(tempDir.toFile());
                log.info("已删除任务临时目录: taskId={}, dir={}", taskId, tempDir);
            }
        }
    }

    @Override
    public void cleanupAllExpiredTempFiles() throws IOException {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(taskTtlDays);

        // 1. 清理过期任务记录对应的文件
        List<ExportTask> expiredTasks = baseMapper.selectExpiredTasks(expireTime);
        for (ExportTask task : expiredTasks) {
            if (StrUtil.isNotBlank(task.getFilePath())) {
                Path filePath = Paths.get(task.getFilePath());
                if (Files.exists(filePath)) {
                    try {
                        Files.delete(filePath);
                        log.info("已删除过期任务文件: taskId={}, file={}", task.getTaskId(), task.getFilePath());
                    } catch (IOException e) {
                        log.warn("删除文件失败: {}", task.getFilePath(), e);
                    }
                }
            }
        }

        // 2. 清理临时目录
        Path tempDir = Paths.get(tempPath);
        if (Files.exists(tempDir)) {
            File[] tempFiles = tempDir.toFile().listFiles();
            if (tempFiles != null) {
                for (File file : tempFiles) {
                    if (file.isDirectory()) {
                        // 检查目录是否过期（按目录名中的时间戳判断）
                        if (isTempDirExpired(file.getName())) {
                            FileUtils.deleteDirectory(file);
                            log.info("已删除过期临时目录: {}", file.getAbsolutePath());
                        }
                    } else if (file.isFile()) {
                        // 检查文件是否过期
                        if (isFileExpired(file)) {
                            Files.delete(file.toPath());
                            log.info("已删除过期临时文件: {}", file.getAbsolutePath());
                        }
                    }
                }
            }
        }

        // 3. 可选：清理空目录
        cleanEmptyDirectories(tempDir);
    }

    /**
     * 清理空目录
     */
    private void cleanEmptyDirectories(Path directory) throws IOException {
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            File[] files = directory.toFile().listFiles();
            if (files != null && files.length == 0) {
                Files.delete(directory);
                log.info("已删除空目录: {}", directory);
            } else if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        cleanEmptyDirectories(file.toPath());
                    }
                }

                // 再次检查目录是否变空
                files = directory.toFile().listFiles();
                if (files != null && files.length == 0) {
                    Files.delete(directory);
                    log.info("已删除变空的目录: {}", directory);
                }
            }
        }
    }

    /**
     * 检查临时目录是否过期
     */
    private boolean isTempDirExpired(String dirName) {
        try {
            // 假设目录名格式: export_任务ID_时间戳
            if (dirName.startsWith(EXPORT_FILE_PREFIX)) {
                String[] parts = dirName.split("_");
                if (parts.length >= 3) {
                    String timestampStr = parts[parts.length - 1];
                    LocalDateTime dirTime = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern(PURE_DATE_PATTERN));
                    return dirTime.isBefore(LocalDateTime.now().minusDays(taskTtlDays));
                }
            }
        } catch (Exception e) {
            log.warn("解析目录名时间失败: {}", dirName, e);
        }
        return false;
    }

    /**
     * 检查文件是否过期
     */
    private boolean isFileExpired(File file) {
        try {
            long lastModified = file.lastModified();
            long expireTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(taskTtlDays);
            return lastModified < expireTime;
        } catch (Exception e) {
            log.warn("检查文件过期时间失败: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

}
