package cn.getech.base.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 11030
 */
@Data
@TableName("GRAPH_THREAD")
@Accessors(chain = true)
public class GraphThread {

    @TableId("thread_id")
    private String threadId;

    @TableField("thread_name")
    private String threadName;

    @TableField("is_released")
    private Integer isReleased;

}