package cn.example.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SortDirectionEnum {

    DESC("desc", "降序"),

    ASC("asc", "升序"),

    ;

    private String id;

    private String text;

    public static String getDefaultId(String id){
        if(ASC.id.equalsIgnoreCase(id) || DESC.id.equalsIgnoreCase(id)){
            return id.toLowerCase(Locale.ROOT);
        }
        return DESC.id;
    }

}
