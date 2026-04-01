package cn.example.ai.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 11030
 */
@Data
@TableName("GRAPH_CHECKPOINT")
@Accessors(chain = true)
public class GraphCheckPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("checkpoint_id")
    private String checkpointId;

    @TableField("thread_id")
    private String threadId;

    @TableField("node_id")
    private String nodeId;

    @TableField("next_node_id")
    private String nextNodeId;

    @TableField("state_data")
    private String stateData;

    @TableField("saved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime savedAt;

}