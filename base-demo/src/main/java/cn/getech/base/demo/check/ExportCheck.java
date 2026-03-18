package cn.getech.base.demo.check;

import cn.getech.base.demo.entity.ExportTask;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import static cn.getech.base.demo.constant.FieldValueConstant.REDIS_TASK_STATUS_PREFIX;
import static cn.getech.base.demo.enums.ExportTaskStatusEnum.CANCELLED;

@Component
public class ExportCheck {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查任务是否被取消
     */
    public boolean isTaskCancelled(String taskId) {
        String statusKey = REDIS_TASK_STATUS_PREFIX + taskId;
        Object obj = redisTemplate.opsForValue().get(statusKey);
        if(obj == null){
            return false;
        }
        ExportTask exportTask = JSONObject.parseObject(obj.toString(), ExportTask.class);
        return exportTask != null && CANCELLED.getId().equals(exportTask.getTaskStatus());
    }

}
