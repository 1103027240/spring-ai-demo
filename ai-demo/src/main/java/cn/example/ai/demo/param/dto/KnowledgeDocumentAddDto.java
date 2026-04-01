package cn.example.ai.demo.param.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "知识库文档批量新增请求参数")
public class KnowledgeDocumentAddDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档列表", example = "文档列表")
    private List<KnowledgeDocumentDto> documents;

}
