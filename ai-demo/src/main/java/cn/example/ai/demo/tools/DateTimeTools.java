package cn.example.ai.demo.tools;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import java.util.Date;

/**
 * @author 11030
 */
@Slf4j
public class DateTimeTools {

    @Tool(name = "getCurrentTime", description = "获取当前时间")
    public String getCurrentTime() {
        return DateUtil.formatDateTime(new Date());
    }

    @Tool(name = "setAlarm", description = "设置当前闹钟")
    public void setAlarm(@ToolParam(description = "当前闹钟时间") String time) {
        log.info("闹钟设置为: {}", time);
    }

}
