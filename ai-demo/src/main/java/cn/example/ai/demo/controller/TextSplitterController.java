package cn.example.ai.demo.controller;

import cn.example.ai.demo.service.TextSplitterService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
@Slf4j
@Tag(name = "文本分割接口", description = "文本分割接口")
@RequestMapping("/textSplitter")
@RestController
public class TextSplitterController {

    @Autowired
    private TextSplitterService textSplitterService;

    @Operation(summary = "指定算法分割文本", description = "指定算法分割文本")
    @PostMapping("/split")
    public void split(@RequestBody Map<String, Object> dto) {
        String algorithm = (String) dto.get("algorithm");
        String text = (String) dto.get("text");
        Map<String, Object> metadata = (Map<String, Object>) dto.get("metadata");
        List<Document> list = textSplitterService.split(text, algorithm, metadata);
        log.info("返回结果: {}", JSONObject.toJSONString(list));
    }

    @Operation(summary = "智能分割文本", description = "智能分割文本")
    @PostMapping("/intelligentSplit")
    public void intelligentSplit(@RequestBody Map<String, Object> dto) {
        String text = "# Markdown标题\n\n" +
                "这是一段普通文本，包含一个HTML标签：<b>粗体文本</b>\n\n" +
                "```java\n" +
                "public class Example {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}\n" +
                "```\n\n" +
                "还有一段JSON：{\"name\": \"test\", \"value\": 123}";
        Map<String, Object> metadata = (Map<String, Object>) dto.get("metadata");
        List<Document> list = textSplitterService.intelligentSplit(text, metadata);
        log.info("返回结果: {}", JSONObject.toJSONString(list));
    }

    @Operation(summary = "批量分割文本", description = "批量分割文本")
    @PostMapping("/batchSplit")
    public void batchSplit(@RequestBody Map<String, Object> dto) {
        String algorithm = (String) dto.get("algorithm");
        Map<String, String> texts = (Map<String, String>) dto.get("texts");
        Map<String, List<Document>> map = textSplitterService.batchSplit(texts, algorithm);
        log.info("返回结果: {}", JSONObject.toJSONString(map));
    }

}
