package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SentimentAnalysisEnum {

    POSITIVE("positive", "积极", "积极（表达满意、感谢、赞扬等）"),

    NEUTRAL("neutral", "中性", "中性（客观询问、陈述事实等）"),

    NEGATIVE("negative", "消极", "消极（表达不满、抱怨、愤怒等）"),

    URGENT("urgent", "紧急", "紧急（表达急切、需要立即处理）"),

    ;

    private String id;

    private String description;

    private String detailDescription;

}
