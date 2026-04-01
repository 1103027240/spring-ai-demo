package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CursorSortByEnum {

    SCORE("score", "分数"),

    CREATE_TIME("createTime", "创建时间"),

    DOC_ID("docId", "文档ID"),

    ;

    private String id;

    private String text;

    public static boolean isValidSortField(String id) {
        return Arrays.asList(SCORE.id, CREATE_TIME.id, DOC_ID.id).contains(id);
    }

    public static CursorSortByEnum getCursorSort(String id){
        return Arrays.stream(CursorSortByEnum.values())
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

}
