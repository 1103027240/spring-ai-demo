package cn.example.base.demo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 提示词Prompt模版属性配置
 * @author 11030
 */
@Data
@Component
@ConfigurationProperties(prefix = "prompt.template")
public class TemplateProperties {

    private String roleSystemTemplate;

    private String roleAssistantTemplate;

    private String roleCombineSystemTemplate;

    private String roleCombineUserTemplate;

    private String demoStudentTemplate;

}
