package cn.getech.base.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作流执行请求DTO
 * @author 11030
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WorkflowRequestDto {

    /**
     * 用户输入
     */
    private String userInput;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

}
