package cn.getech.base.demo.service.writer;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchVO;
import cn.getech.base.demo.dto.KnowledgeDocumentVO;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CsvExportWriter extends ExportWriter {

    private final OutputStream outputStream;
    private final NonClosingWriter writer;
    private final CsvMapper csvMapper;
    private final CsvSchema schema;
    private volatile boolean closed = false;
    private volatile boolean initialized = false;

    public CsvExportWriter(OutputStream outputStream, KnowledgeDocumentExportDto dto) {
        super(dto);

        this.outputStream = outputStream;

        // 创建 OutputStreamWriter 并包装成不自动关闭的 Writer
        OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.writer = new NonClosingWriter(osw);

        // 配置CSV写入器
        this.csvMapper = new CsvMapper();
        this.csvMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        this.csvMapper.configure(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING, true);

        // 创建CSV Schema（使用英文字段名，与 KnowledgeDocumentSearchVO 类的字段匹配）
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
                .addColumn("createTime")
                .build();

        // 不使用 withHeader()，我们将手动写入中文表头
        this.schema = schema.withoutHeader();
    }

    @Override
    public void init() throws IOException {
        if (initialized) {
            log.warn("CSV导出写入器已经初始化，跳过");
            return;
        }

        // 写入 UTF-8 BOM（字节顺序标记），确保 Excel 正确识别中文编码
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        outputStream.write(bom);

        // 手动写入中文表头
        if (Boolean.TRUE.equals(dto.getIncludeHeader())) {
            String header = "文档ID,标题,内容,摘要,分类ID,关键词,标签,来源,作者,相似度分数,创建时间";
            writer.write(header);
            writer.write("\n");
        }

        initialized = true;
        log.debug("CSV导出写入器初始化完成");
    }

    @Override
    public void writeBatch(List<KnowledgeDocumentVO> documents) throws IOException {
        if (closed) {
            throw new IOException("Writer已关闭，无法写入数据");
        }
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 确保已初始化（写入BOM）
        if (!initialized) {
            init();
        }

        // 转换数据
        List<KnowledgeDocumentSearchVO> exportData = documents.stream()
                .map(KnowledgeDocumentSearchVO::fromKnowledgeDocumentVO)
                .collect(Collectors.toList());

        // 写入数据
        try {
            for (KnowledgeDocumentSearchVO data : exportData) {
                csvMapper.writer(schema).writeValue(writer, data);
            }

            // 刷新缓冲区
            if (writer != null && !closed) {
                writer.flush();
            }
        } catch (IOException e) {
            closed = true;
            throw e;
        }

        log.debug("写入CSV批次数据: {}条", exportData.size());
    }

    @Override
    public void finish() throws IOException {
        if (closed) {
            log.warn("Writer已关闭，无法完成导出");
            return;
        }
        if (writer != null) {
            try {
                writer.flush();
                log.debug("CSV导出完成");
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

        // 在关闭前确保调用 finish()，保证数据完整
        try {
            finish();
        } catch (IOException e) {
            log.warn("在关闭时完成导出失败，将继续关闭流", e);
        }

        // 关闭底层输出流
        if (writer != null) {
            closed = true;
            try {
                writer.reallyClose();
                log.debug("CSV写入器已关闭");
            } catch (IOException e) {
                log.error("关闭CSV写入器失败", e);
                throw e;
            }
        }
    }

}
