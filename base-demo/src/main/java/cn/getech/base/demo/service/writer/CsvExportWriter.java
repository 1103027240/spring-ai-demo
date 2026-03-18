package cn.getech.base.demo.service.writer;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchVO;
import cn.getech.base.demo.dto.KnowledgeDocumentVO;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CsvExportWriter extends ExportWriter {

    private final Writer writer;
    private final ObjectWriter csvWriter;
    private boolean headerWritten = false;

    public CsvExportWriter(OutputStream outputStream, KnowledgeDocumentExportDto exportDto) {
        super(exportDto);

        this.writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        // 配置CSV写入器
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        csvMapper.configure(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING, true);

        // 创建CSV Schema
        CsvSchema schema = CsvSchema.builder()
                .addColumn("docId")
                .addColumn("title")
                .addColumn("content")
                .addColumn("summary")
                .addColumn("categoryId")
                .addColumn("keywords")
                .addColumn("tags")
                .addColumn("source")
                .addColumn("author")
                .addColumn("score")
                .addColumn("createTimeStr")
                .build();

        if (Boolean.TRUE.equals(exportDto.getIncludeHeader())) {
            schema = schema.withHeader();
        }

        this.csvWriter = csvMapper.writer(schema);
    }

    @Override
    public void init() throws IOException {
        // CSV写入器初始化
        log.debug("CSV导出写入器初始化完成");
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
            csvWriter.writeValue(writer, data);
        }

        // 刷新缓冲区
        writer.flush();

        log.debug("写入CSV批次数据: {}条", exportData.size());
    }

    @Override
    public void finish() throws IOException {
        if (writer != null) {
            writer.flush();
            log.debug("CSV导出完成");
        }
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

}
