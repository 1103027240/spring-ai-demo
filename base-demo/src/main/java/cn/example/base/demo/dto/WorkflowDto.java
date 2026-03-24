package cn.example.base.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 工作流执行请求DTO
 * @author 11030
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WorkflowDto implements Serializable {
    private static final long serialVersionUID = 1L;

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
