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

    /**
     * 页面url
     */
    private String url;

    /**
     * 页面url访问次数
     */
    private Long count;

    /**
     * 窗口开始时间
     */
    private Long windowStart;

    /**
     * 窗口结束时间
     */
    private Long windowEnd;

    @Override
    public String toString() {
        return "UrlViewDto{" +
                "url='" + url + '\'' +
                ", count=" + count +
                ", windowStart=" + new Timestamp(windowStart) +
                ", windowEnd=" + new Timestamp(windowEnd) +
                '}';
    }

}
