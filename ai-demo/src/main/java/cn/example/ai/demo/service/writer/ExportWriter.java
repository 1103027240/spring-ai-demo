package cn.example.ai.demo.service.writer;

import cn.example.ai.demo.param.dto.KnowledgeDocumentExportDto;
import cn.example.ai.demo.param.vo.KnowledgeDocumentVO;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * 导出写入器抽象类
 */
public abstract class ExportWriter implements Closeable {

    protected final KnowledgeDocumentExportDto dto;

    protected ExportWriter(KnowledgeDocumentExportDto dto) {
        this.dto = dto;
    }

    /**
     * 初始化写入器
     */
    public abstract void init() throws IOException;

    /**
     * 写入批次数据
     */
    public abstract void writeBatch(List<KnowledgeDocumentVO> documents) throws IOException;

    /**
     * 完成写入
     */
    public abstract void finish() throws IOException;

}
