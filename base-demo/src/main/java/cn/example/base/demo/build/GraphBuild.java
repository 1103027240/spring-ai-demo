package cn.example.base.demo.build;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import org.springframework.stereotype.Component;

/**
 * @author 11030
 */
@Component
public class GraphBuild {

    public GraphRepresentation buildGraphRepresentation(StateGraph stateGraph, String title) {
        return stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, title);
    }

    public CompileConfig buildCompileConfig(MysqlSaver mySqlSaver, boolean interruptFlag, String... interruptBefore){
        CompileConfig.Builder builder = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(mySqlSaver) // Mysql状态存储
                        .build());
        if (BooleanUtil.isTrue(interruptFlag)) {
            builder.interruptBefore(interruptBefore); // 在此节点前中断
        }
        return builder.build();
    }

}
