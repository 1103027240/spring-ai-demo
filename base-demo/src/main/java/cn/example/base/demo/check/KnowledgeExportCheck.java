package cn.example.base.demo.check;

import cn.example.base.demo.entity.ExportTask;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import static cn.example.base.demo.constant.RedisKeyConstant.EXPORT_TASK_PREFIX;
import static cn.example.base.demo.enums.ExportTaskStatusEnum.CANCELLED;

@Component
public class KnowledgeExportCheck {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查任务是否被取消
     */
    public boolean isTaskCancelled(String taskId) {
        String statusKey = EXPORT_TASK_PREFIX + taskId;
        Object obj = redisTemplate.opsForValue().get(statusKey);
        if(obj == null){
            return false;
        }

        ExportTask exportTask = JSONObject.parseObject(obj.toString(), ExportTask.class);
        return exportTask != null && CANCELLED.getId().equals(exportTask.getTaskStatus());
    }

}
