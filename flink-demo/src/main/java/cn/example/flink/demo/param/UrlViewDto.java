package cn.example.flink.demo.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlViewDto {

    private String url;

    private Long count;

    private Long startTime;

    private Long endTime;

    @Override
    public String toString() {
        return "UrlViewDto{" +
                "url='" + url + '\'' +
                ", count=" + count +
                ", startTime=" + new Timestamp(startTime) +
                ", endTime=" + new Timestamp(endTime) +
                '}';
    }

}
