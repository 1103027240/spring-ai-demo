package cn.example.base.demo.check;

import cn.example.base.demo.param.dto.TextSegmentDto;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import java.util.ArrayList;
import java.util.List;
import static cn.example.base.demo.enums.SplitterTypeEnum.*;

/**
 * 检测文本中的类型分段
 * @author 11030
 */
public class DetectTextSegmentCheck {

    /**
     * 检测文本中的类型分段
     */
    public static List<TextSegmentDto> detectTextSegments(String text) {
        List<TextSegmentDto> segments = new ArrayList<>();
        if (StrUtil.isBlank(text)) {
            return segments;
        }

        // 1. 检测Markdown代码块（最高优先级）
        if (processCodeBlocks(text, segments)) {
            return segments;
        }

        // 2. 检测HTML结构（第二优先级）
        if (DetectTextTypeCheck.isHtml(text)) {
            segments.add(new TextSegmentDto(text, HTML.getId()));
            return segments;
        }

        // 3. 检测JSON结构（第三优先级）
        if (JSONUtil.isTypeJSON(text)) {
            segments.add(new TextSegmentDto(text, JSON.getId()));
            return segments;
        }

        // 4. 检测Markdown（第四优先级）
        if (DetectTextTypeCheck.isMarkdown(text)) {
            segments.add(new TextSegmentDto(text, MARKDOWN.getId()));
            return segments;
        }

        // 5. 按段落分割（保留段落结构）
        String[] paragraphs = text.split("\\n\\s*\\n");
        if (paragraphs.length > 1) {
            for (String paragraph : paragraphs) {
                if (StrUtil.isNotBlank(paragraph)) {
                    // 对每个段落进行类型检测
                    String type = DetectTextTypeCheck.detectTextType(paragraph);
                    segments.add(new TextSegmentDto(paragraph, type));
                }
            }
            return segments;
        }

        // 6. 按行分割文本，逐行检测类型
        String[] lines = text.split("\\n");
        if (lines.length == 0) {
            segments.add(new TextSegmentDto(text, DetectTextTypeCheck.detectTextType(text)));
            return segments;
        }

        // 7. 合并连续相同类型的行
        mergeSameTypeLines(lines, segments);

        return segments;
    }

    /**
     * 处理代码块
     * @param text 待处理的文本
     * @param segments 分段列表
     * @return 是否处理了代码块
     */
    private static boolean processCodeBlocks(String text, List<TextSegmentDto> segments) {
        int codeBlockStart = text.indexOf("```");
        if (codeBlockStart != -1) {
            int codeBlockEnd = text.indexOf("```", codeBlockStart + 3);
            if (codeBlockEnd != -1) {
                // 代码块前的部分
                if (codeBlockStart > 0) {
                    String prefix = text.substring(0, codeBlockStart);
                    segments.addAll(detectTextSegments(prefix));
                }

                // 代码块部分
                String codeBlock = text.substring(codeBlockStart, codeBlockEnd + 3);
                segments.add(new TextSegmentDto(codeBlock, CODE.getId()));

                // 代码块后的部分
                if (codeBlockEnd + 3 < text.length()) {
                    String suffix = text.substring(codeBlockEnd + 3);
                    segments.addAll(detectTextSegments(suffix));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 合并连续相同类型的行
     * @param lines 行数组
     * @param segments 分段列表
     */
    private static void mergeSameTypeLines(String[] lines, List<TextSegmentDto> segments) {
        StringBuilder currentSegment = new StringBuilder();
        String currentType = DetectTextTypeCheck.detectTextType(lines[0]);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String lineType = DetectTextTypeCheck.detectTextType(line);

            // 如果当前行类型与之前的类型不同，保存当前分段并开始新分段
            if (!lineType.equals(currentType) && currentSegment.length() > 0) {
                segments.add(new TextSegmentDto(currentSegment.toString(), currentType));
                currentSegment.setLength(0);
                currentType = lineType;
            }

            currentSegment.append(line);
            if (i < lines.length - 1) {
                currentSegment.append("\n");
            }
        }

        // 保存最后一个分段
        if (currentSegment.length() > 0) {
            segments.add(new TextSegmentDto(currentSegment.toString(), currentType));
        }
    }

}
