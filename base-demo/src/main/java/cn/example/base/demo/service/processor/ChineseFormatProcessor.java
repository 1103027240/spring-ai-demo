package cn.example.base.demo.service.processor;

import cn.example.base.demo.service.TextSplitterFormatProcessor;
import org.springframework.stereotype.Service;

/**
 * 中文文本分割处理器
 * @author 11030
 */
@Service
public class ChineseFormatProcessor implements TextSplitterFormatProcessor {

    @Override
    public String process(String text) {
        return text;
    }

    @Override
    public void append(StringBuilder sb, String section) {
        if (sb.length() > 0 && !sb.toString().endsWith(" ")) {
            sb.append(" ");
        }
        sb.append(section);
    }

}
