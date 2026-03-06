package cn.getech.base.demo.service.processor;

import cn.getech.base.demo.service.TextSplitterFormatProcessor;
import org.springframework.stereotype.Service;

/**
 * 代码分割处理器
 * @author 11030
 */
@Service
public class CodeFormatProcessor implements TextSplitterFormatProcessor {

    @Override
    public String process(String text) {
        return text;
    }

    @Override
    public void append(StringBuilder sb, String section) {
        sb.append(section);
    }

}
