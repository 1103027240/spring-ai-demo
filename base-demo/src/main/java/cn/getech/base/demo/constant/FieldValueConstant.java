package cn.getech.base.demo.constant;

import java.util.regex.Pattern;

/**
 * @author 11030
 */
public class FieldValueConstant {

    public static final String DEEPSEEK_API_KEY = "sk-b8a6686cd78144ab8d2983937429a864";

    // 订单号正则表达式
    public static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("([A-Z]{2,6}\\d{6,12})|(\\d{6,12})");

    // 服务单号正则表达式
    public static final Pattern SERVICE_NUMBER_PATTERN = Pattern.compile("[A-Z]{2}\\d{8,12}");


}
