package cn.getech.base.demo.controller;

import cn.getech.base.demo.service.ToolCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 11030
 */
@Tag(name = "ToolCall调用接口", description = "ToolCall调用相关API")
@RequestMapping("/toolCall")
@RestController
public class ToolCallController {

    @Autowired
    private ToolCallService toolCallService;

    @Operation(summary = "获取当前时间", description = "获取当前时间")
    @GetMapping("/getCurrentTime")
    public String getCurrentTime(
            @Parameter(description = "用户消息内容", required = true, example = "获取当前时间")
            @RequestParam(value = "msg") String msg){
        return toolCallService.getCurrentTime(msg);
    }

    @Operation(summary = "获取城市天气", description = "获取城市天气")
    @GetMapping("/getCityWeather")
    public String getCityWeather(
            @Parameter(description = "用户消息内容", required = true, example = "获取北京天气")
            @RequestParam(value = "msg") String msg){
        return toolCallService.getCityWeather(msg);
    }

}
