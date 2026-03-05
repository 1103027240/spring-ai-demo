package cn.getech.spring.ai.demo.controller;

import cn.getech.spring.ai.demo.service.RagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author 11030
 */
@Tag(name = "RAG检索增强接口", description = "RAG检索增强相关API")
@RequestMapping("/rag")
@RestController
public class RagController {

    @Autowired
    private RagService ragService;

    @Operation(summary = "新增文档", description = "新增文档")
    @PostMapping("/addDocument")
    public void addDocument(
            @Parameter(description = "用户消息内容", required = true, example = "1加1等于几")
            @RequestParam(value = "msg") String msg){
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
        ragService.addDocument(text, null, null);
    }

    @Operation(summary = "查询文档", description = "查询文档")
    @PostMapping("/search")
    public List<Document> search(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg){
        return ragService.search(msg);
    }

    @Operation(summary = "分页查询文档", description = "分页查询文档")
    @GetMapping("/pageSearch")
    public Page<Document> pageSearch(
            @Parameter(description = "用户消息内容", required = true, example = "你好")
            @RequestParam(value = "msg") String msg,
            @Parameter(description = "当前页", required = true, example = "1")
            @RequestParam(value = "page") Integer page,
            @Parameter(description = "每页数", required = true, example = "10")
            @RequestParam(value = "size") Integer size){
        return ragService.pageSearch(msg, page, size);
    }

}
