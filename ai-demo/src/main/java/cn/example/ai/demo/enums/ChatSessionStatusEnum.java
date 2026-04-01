package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ChatSessionStatusEnum {

    ENDED(0, "已结束"),

    ACTIVE(1, "进行中"),

    TRANSFERRED(2, "已转人工"),

    WAITING(3, "等待回复"),

    ;

    private Integer id;

    private String text;

}
