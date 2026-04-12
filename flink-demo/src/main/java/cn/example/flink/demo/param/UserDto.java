package cn.example.flink.demo.param;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Builder
@Data
public class UserDto {

    private String userId;

    private String userName;

    private String phone;

    private Integer age;

    private Integer sex;

    private String email;

    private BigDecimal salary;

}
