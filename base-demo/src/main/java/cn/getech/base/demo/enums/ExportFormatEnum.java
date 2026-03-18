package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExportFormatEnum {

    EXCEL("EXCEL", ".xlsx", ".xlsx.gz"),

    CSV("CSV", ".csv", ".csv.gz"),

    JSON("JSON", ".json", ".json.gz"),

    GZIP("GZIP", "", ".gz"),

    ;

    private String id;

    private String text;

    private String detailText;

}
