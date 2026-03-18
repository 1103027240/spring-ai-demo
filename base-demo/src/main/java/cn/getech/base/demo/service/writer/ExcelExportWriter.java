package cn.getech.base.demo.service.writer;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchVO;
import cn.getech.base.demo.dto.KnowledgeDocumentVO;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ExcelExportWriter extends ExportWriter {

    private final ExcelWriter excelWriter;
    private final WriteSheet writeSheet;
    private int sheetRowCount = 0;
    private int currentSheetIndex = 1;
    private static final int MAX_ROWS_PER_SHEET = 100000;

    public ExcelExportWriter(OutputStream outputStream, KnowledgeDocumentExportDto dto) {
        super(dto);

        // 创建Excel写入器
        this.excelWriter = EasyExcel.write(outputStream, KnowledgeDocumentSearchVO.class)
                .inMemory(Boolean.TRUE.equals(dto.getIncludeHeader()))
                .build();

        // 创建第一个Sheet
        this.writeSheet = EasyExcel.writerSheet("Sheet1")
                .needHead(Boolean.TRUE.equals(dto.getIncludeHeader()))
                .build();
    }

    @Override
    public void init() throws IOException {
        // Excel写入器初始化
        log.debug("Excel导出写入器初始化完成");
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

        // 检查是否需要新建Sheet
        if (sheetRowCount + exportData.size() > MAX_ROWS_PER_SHEET) {
            // 当前Sheet已满，创建新Sheet
            currentSheetIndex++;
            WriteSheet newSheet = EasyExcel.writerSheet("Sheet" + currentSheetIndex)
                    .needHead(Boolean.TRUE.equals(dto.getIncludeHeader()))
                    .build();

            excelWriter.write(exportData, newSheet);
            sheetRowCount = exportData.size();
        } else {
            // 写入当前Sheet
            excelWriter.write(exportData, writeSheet);
            sheetRowCount += exportData.size();
        }

        log.debug("写入Excel批次数据: {}条", exportData.size());
    }

    @Override
    public void finish() throws IOException {
        if (excelWriter != null) {
            excelWriter.finish();
            log.debug("Excel导出完成，共{}个Sheet", currentSheetIndex);
        }
    }

    @Override
    public void close() throws IOException {
        finish();
    }

}