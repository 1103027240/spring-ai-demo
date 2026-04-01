package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CodeSearchNodeEnum {

    PRE_PROCESS_NODE("preProcessNode", "搜索前"),

    ROUTING_NODE("routingNode", "搜索中"),

    POST_PROCESS_NODE("postProcessNode", "搜索后"),

    ;

    private String id;

    private String text;

}
