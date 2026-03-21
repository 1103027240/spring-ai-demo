package cn.getech.base.demo.constant;

/**
 * @author 11030
 */
public class RedisKeyConstant {

    public static final String WORKFLOW_CUSTOMER_PREFIX = "ai_customer:";

    public static final String SYNC_TASKS = WORKFLOW_CUSTOMER_PREFIX + "sync:tasks";

    public static final String SESSION_ACTIVE = WORKFLOW_CUSTOMER_PREFIX + "session:active:";

    public static final String CUSTOMER_KNOWLEDGE_PREFIX = "customer:knowledge:";

    public static final String REDIS_TASK_STATUS_PREFIX = "export:task:status:";

}
