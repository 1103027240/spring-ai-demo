package cn.example.agent.demo.function;

import java.util.Map;
import java.util.function.Function;

/**
 * 天气查询函数
 * @author 11030
 */
public class WeatherFunction implements Function<Map<String, Object>, String> {

    @Override
    public String apply(Map<String, Object> params) {
        String city = params.get("message") != null ? params.get("message").toString() : "未知城市";
        return String.format("%s：天气是小雨多云，微风清爽", city);
    }

}
