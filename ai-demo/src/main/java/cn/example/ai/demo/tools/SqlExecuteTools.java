package cn.example.ai.demo.tools;

import cn.example.ai.demo.context.UserContext;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * @author 11030
 */
public class SqlExecuteTools {

    @Tool(name = "query", description = "查询数据库数据")
    public String query(@ToolParam(name = "sql", description = "sql语句") String sql, UserContext userContext) {
        return String.format("用户[%s]执行了一条sql：[%s]", userContext.getUserId(), sql);
    }

}
