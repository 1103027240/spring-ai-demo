package cn.getech.mcp.server.demo.service;

import cn.getech.mcp.server.demo.dto.WeatherForecastDto;

public interface WeatherService {

    String getWeather(String city);

    WeatherForecastDto getForecast(String city, int days);

}
