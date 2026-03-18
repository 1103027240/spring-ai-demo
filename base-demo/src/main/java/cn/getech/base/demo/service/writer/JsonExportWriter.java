package cn.getech.base.demo.service.writer;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchVO;
import cn.getech.base.demo.dto.KnowledgeDocumentVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JsonExportWriter extends ExportWriter {

    private final Writer writer;
    private final ObjectMapper objectMapper;
    private boolean firstItem = true;
    private volatile boolean closed = false;

    public JsonExportWriter(OutputStream outputStream, KnowledgeDocumentExportDto dto) {
        super(dto);

        this.writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Override
    public void init() throws IOException {
        if (closed) {
            throw new IOException("Writer已关闭，无法初始化");
        }
        // 写入JSON数组开始符号
        try {
            writer.write("[\n");
            log.debug("JSON导出写入器初始化完成");
        } catch (IOException e) {
            closed = true;
            throw e;
        }
    }

    @Override
    public void writeBatch(List<KnowledgeDocumentVO> documents) throws IOException {
        if (closed) {
            throw new IOException("Writer已关闭，无法写入数据");
        }
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 转换数据
        List<KnowledgeDocumentSearchVO> exportData = documents.stream()
                .map(KnowledgeDocumentSearchVO::fromKnowledgeDocumentVO)
                .collect(Collectors.toList());

        // 写入数据
        try {
            for (KnowledgeDocumentSearchVO data : exportData) {
                if (!firstItem) {
                    writer.write(",\n");
                }

                String jsonStr = objectMapper.writeValueAsString(data);
                writer.write("  " + jsonStr);

                firstItem = false;
            }

            // 刷新缓冲区
            if (writer != null && !closed) {
                writer.flush();
            }
        } catch (IOException e) {
            closed = true;
            throw e;
        }

        log.debug("写入JSON批次数据: {}条", exportData.size());
    }

    @Override
    public void finish() throws IOException {
        if (closed) {
            log.warn("Writer已关闭，无法完成导出");
            return;
        }
        if (writer != null) {
            try {
                writer.write("\n]");
                writer.flush();
                log.debug("JSON导出完成");
            } catch (IOException e) {
                // 注意：这里不设置 closed 标志，让 close() 方法来处理
                throw e;
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            log.debug("Writer已经关闭，无需重复关闭");
            return;
        }
        
        // 在关闭前确保调用 finish()，保证 JSON 格式完整（写入结尾的 ]）
        try {
            finish();
        } catch (IOException e) {
            log.warn("在关闭时完成导出失败，将继续关闭流", e);
        }
        
        if (writer != null) {
            closed = true;
            try {
                writer.close();
                log.debug("JSON写入器已关闭");
            } catch (IOException e) {
                log.error("关闭JSON写入器失败", e);
                throw e;
            }
        }
    }

}
