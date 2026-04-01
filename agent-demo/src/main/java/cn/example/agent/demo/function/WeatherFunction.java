package cn.example.agent.demo.function;

import java.util.function.Function;

/**
 * @author 11030
 */
public class WeatherFunction implements Function<String, String> {

    @Override
    public String apply(String city) {
        return String.format("%s：天气是小雨多云，微风清爽", city);
    }

}
