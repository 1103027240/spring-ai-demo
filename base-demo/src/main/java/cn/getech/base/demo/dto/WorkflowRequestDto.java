package cn.getech.base.demo.dto;

import lombok.Data;

/**
 * 工作流执行请求DTO
 * @author 11030
 */
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


    public WorkflowRequestDto() {

    }

    public WorkflowRequestDto(String userInput, Long userId, String userName) {
        this.userInput = userInput;
        this.userId = userId;
        this.userName = userName;
    }

}
