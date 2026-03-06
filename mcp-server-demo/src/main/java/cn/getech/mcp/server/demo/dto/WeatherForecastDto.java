package cn.getech.mcp.server.demo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author 11030
 */
@Data
@Builder
public class WeatherForecastDto {

    private String city;

    private int days;

    private String weather;

    private String temperature;

    private String suggestion;

}
