package cn.getech.base.demo.controller;

import cn.getech.base.demo.entity.StudentRecord;
import cn.getech.base.demo.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
@Tag(name = "Prompt提示词接口", description = "Prompt提示词相关API")
@RequestMapping("/prompt")
@RestController
public class PromptController {

    @Autowired
    private PromptService promptService;

    @Operation(summary = "系统角色", description = "系统角色")
    @GetMapping("/role/system")
    public Flux<String> doChatRoleSystem(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return promptService.doChatRoleSystem(msg);
    }

    @Operation(summary = "Tool角色", description = "Tool角色")
    @GetMapping("/role/tool")
    public Flux<String> doChatRoleTool(
            @Parameter(description = "用户消息内容", required = true, example = "北京")
            @RequestParam(value = "msg") String msg){
        return promptService.doChatRoleTool(msg);
    }

    @Operation(summary = "组合角色", description = "组合角色")
    @GetMapping("/role/combine")
    public Flux<String> doChatRoleCombine(
            @Parameter(description = "用户消息内容", required = true, example = "新能源汽车")
            @RequestParam(value = "msg") String msg){
        return promptService.doChatRoleCombine(msg);
    }

    @Operation(summary = "格式化输出", description = "格式化输出")
    @GetMapping("/chat/output")
    public StudentRecord doChatOutput(
            @Parameter(description = "姓名", required = true, example = "张三")
            @RequestParam(value = "name") String name,
            @Parameter(description = "编号", required = true, example = "123456")
            @RequestParam(value = "id") String id,
            @Parameter(description = "年龄", required = true, example = "17")
            @RequestParam(value = "age") Integer age,
            @Parameter(description = "邮箱", required = true, example = "123456@qq.com")
            @RequestParam(value = "email") String email){
        return promptService.doChatOutput(name, id, age, email);
    }

}
