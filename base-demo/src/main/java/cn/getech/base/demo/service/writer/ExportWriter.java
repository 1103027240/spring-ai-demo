package cn.getech.base.demo.service.writer;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentVO;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * 导出写入器抽象类
 */
public abstract class ExportWriter implements Closeable {

    protected final KnowledgeDocumentExportDto exportDto;

    protected ExportWriter(KnowledgeDocumentExportDto exportDto) {
        this.exportDto = exportDto;
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
