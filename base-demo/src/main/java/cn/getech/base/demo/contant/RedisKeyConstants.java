package cn.getech.base.demo.contant;

/**
 * @author 11030
 */
public class RedisKeyConstants {

    public static final String WORKFLOW_CUSTOMER_PREFIX = "ai_customer:";

    public static final String WORKFLOW_STATE = WORKFLOW_CUSTOMER_PREFIX + "workflow:state:";

    public static final String EXECUTION_DETAIL = WORKFLOW_CUSTOMER_PREFIX + "execution:detail:";

    public static final String SESSION_HISTORY = WORKFLOW_CUSTOMER_PREFIX + "session:history:";

    public static final String SYNC_TASKS = WORKFLOW_CUSTOMER_PREFIX + "sync:tasks";

    public static final String SESSION_ACTIVE = WORKFLOW_CUSTOMER_PREFIX + "session:active:";

}
