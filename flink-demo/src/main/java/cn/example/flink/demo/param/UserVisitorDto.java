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
public class UserVisitorDto {

    private String userId;

    private String url;

    private Long timestamp;

    @Override
    public String toString() {
        return "UserVisitorDto{" +
                "userId='" + userId + '\'' +
                ", url='" + url + '\'' +
                ", timestamp=" + new Timestamp(timestamp) +
                '}';
    }
}
