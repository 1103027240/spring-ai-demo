package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AfterSalesTypeEnum {

    RETURN_REQUEST(1, "return_request", "退货申请"),

    EXCHANGE_REQUEST(2, "exchange_request", "换货申请"),

    REPAIR_REQUEST(3, "repair_request", "维修申请"),

    REFUND_REQUEST(4, "refund_request", "退款申请"),

    COMPLAINT(5, "complaint", "投诉"),

    PROGRESS_QUERY(6, "progress_query", "进度查询"),

    OTHER(7, "other", "其他"),

    ;

    private Integer code;

    private String text;

    private String detailText;

    public static String getDetailText(Integer code) {
        return Arrays.asList(AfterSalesTypeEnum.values())
                .stream().filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .map(AfterSalesTypeEnum::getDetailText)
                .orElse("未知");
    }

    public static String getName(String description){
        return Arrays.asList(AfterSalesTypeEnum.values())
                .stream().filter(e -> Objects.equals(e.getText(), description))
                .findFirst()
                .map(AfterSalesTypeEnum::name)
                .orElse("");
    }

}
