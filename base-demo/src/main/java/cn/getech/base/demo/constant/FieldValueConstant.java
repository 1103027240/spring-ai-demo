package cn.getech.base.demo.constant;

import java.util.regex.Pattern;

/**
 * @author 11030
 */
public class FieldValueConstant {

    // 订单号正则表达式
    public static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("([A-Z]{2,6}\\d{6,12})|(\\d{6,12})");

    // 服务单号正则表达式
    public static final Pattern SERVICE_NUMBER_PATTERN = Pattern.compile("[A-Z]{2}\\d{8,12}");


}
