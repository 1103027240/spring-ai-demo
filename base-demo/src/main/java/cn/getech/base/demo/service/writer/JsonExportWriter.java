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

    public JsonExportWriter(OutputStream outputStream, KnowledgeDocumentExportDto dto) {
        super(dto);

        this.writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Override
    public void init() throws IOException {
        // 写入JSON数组开始符号
        writer.write("[\n");
        log.debug("JSON导出写入器初始化完成");
    }

    @Override
    public void writeBatch(List<KnowledgeDocumentVO> documents) throws IOException {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 转换数据
        List<KnowledgeDocumentSearchVO> exportData = documents.stream()
                .map(KnowledgeDocumentSearchVO::fromKnowledgeDocumentVO)
                .collect(Collectors.toList());

        // 写入数据
        for (KnowledgeDocumentSearchVO data : exportData) {
            if (!firstItem) {
                writer.write(",\n");
            }

            String jsonStr = objectMapper.writeValueAsString(data);
            writer.write("  " + jsonStr);

            firstItem = false;
        }

        // 刷新缓冲区
        writer.flush();

        log.debug("写入JSON批次数据: {}条", exportData.size());
    }

    @Override
    public void finish() throws IOException {
        if (writer != null) {
            writer.write("\n]");
            writer.flush();
            log.debug("JSON导出完成");
        }
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

}
