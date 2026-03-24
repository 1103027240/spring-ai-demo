package cn.example.mcp.server.demo.service;

import cn.example.mcp.server.demo.dto.WeatherForecastDto;

public interface WeatherService {

    String getWeather(String city);

    WeatherForecastDto getForecast(String city, int days);

}
