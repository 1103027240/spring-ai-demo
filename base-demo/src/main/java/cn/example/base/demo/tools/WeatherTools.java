package cn.example.base.demo.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 11030
 */
public class WeatherTools {

    private static final Map<String, String> CITIES = new HashMap<>();

    static {
        CITIES.put("北京", "110000");
        CITIES.put("上海", "310000");
        CITIES.put("广州", "440100");
        CITIES.put("深圳", "440300");
        CITIES.put("杭州", "330100");
    }

    @Tool(name = "getWeather", description = "输入城市获取天气信息")
    public String getWeather(@ToolParam(name = "city", description = "城市名称") String city) {
        if (CITIES.containsKey(city)) {
            // 这里可以调用实际天气API
            return String.format("%s的天气：晴，温度25°C，湿度60%%，风向东南风3级", city);
        } else {
            return String.format("抱歉，暂不支持[%s]天气查询", city);
        }
    }

}
