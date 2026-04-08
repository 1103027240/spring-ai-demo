package cn.example.flink.demo.job.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVisitorDto {

    private String userId;

    private String url;

    private Long timestamp;

}
