package cn.getech.base.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author 11030
 */
@Data
@TableName("order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_number")
    private String orderNumber;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    private Integer status;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("shipping_address")
    private String shippingAddress;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
