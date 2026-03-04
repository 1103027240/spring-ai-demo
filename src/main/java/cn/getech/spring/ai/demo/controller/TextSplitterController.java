package cn.getech.spring.ai.demo.controller;

import cn.getech.spring.ai.demo.service.TextSplitterService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
@Slf4j
@Tag(name = "文本分割器接口", description = "文本分割器相关API")
@RestController
@RequestMapping("/splitter")
public class TextSplitterController {

    @Autowired
    private TextSplitterService splitterService;

    @Operation(summary = "指定算法分割文本", description = "指定算法分割文本")
    @PostMapping("/algorithm")
    public void split(@RequestBody Map<String, Object> request) {
        String algorithm = (String) request.get("algorithm");
        String text = (String) request.get("text");
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        List<Document> list = splitterService.split(text, algorithm, metadata);
        log.info("返回结果: {}", JSONObject.toJSONString(list));
    }

    @Operation(summary = "智能分割文本", description = "智能分割文本")
    @PostMapping("/intelligent")
    public void intelligentSplit(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        List<Document> list = splitterService.intelligentSplit(text, metadata);
        log.info("返回结果: {}", JSONObject.toJSONString(list));
    }

    @Operation(summary = "批量分割文本", description = "批量分割文本")
    @PostMapping("/batch")
    public void batchSplit(@RequestBody Map<String, Object> request) {
        String algorithm = (String) request.get("algorithm");
        Map<String, String> texts = (Map<String, String>) request.get("texts");
        Map<String, List<Document>> map = splitterService.batchSplit(texts, algorithm);
        log.info("返回结果: {}", JSONObject.toJSONString(map));
    }

}
