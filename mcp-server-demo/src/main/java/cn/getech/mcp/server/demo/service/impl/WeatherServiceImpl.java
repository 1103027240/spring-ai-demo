package cn.getech.mcp.server.demo.service.impl;

import cn.getech.mcp.server.demo.dto.WeatherForecastDto;
import cn.getech.mcp.server.demo.service.WeatherService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 11030
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Map<String, String> CITIES = new HashMap<>();

    static {
        CITIES.put("北京", "110000");
        CITIES.put("上海", "310000");
        CITIES.put("广州", "440100");
        CITIES.put("深圳", "440300");
        CITIES.put("杭州", "330100");
    }

    @Tool(description = "输入城市获取天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        if (CITIES.containsKey(city)) {
            // 这里可以调用实际天气API
            return String.format("%s的天气：晴，温度25°C，湿度60%%，风向东南风3级", city);
        } else {
            return String.format("抱歉，暂不支持%s的天气查询", city);
        }
    }

    @Tool(description = "获取天气预报")
    public WeatherForecastDto getForecast(@ToolParam(description = "城市名称") String city, @ToolParam(description = "预报天数") int days) {
        return WeatherForecastDto.builder()
                .city(city)
                .days(days)
                .weather("晴转多云")
                .temperature("22-28°C")
                .suggestion("适宜出行")
                .build();
    }

}
