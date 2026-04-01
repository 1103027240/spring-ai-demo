package cn.example.ai.demo.service.processor;

import cn.example.ai.demo.service.TextSplitterFormatProcessor;
import org.springframework.stereotype.Service;

/**
 * Markdown分割处理器
 * @author 11030
 */
@Service
public class MarkdownFormatProcessor implements TextSplitterFormatProcessor {

    @Override
    public String process(String text) {
        return text.replaceAll("\\n+$", "\n");
    }

    @Override
    public void append(StringBuilder sb, String section) {
        sb.append(section).append("\n\n");
    }

}
