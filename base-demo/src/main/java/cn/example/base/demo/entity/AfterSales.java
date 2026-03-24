package cn.example.base.demo.entity;

import cn.example.base.demo.enums.AfterSalesStatusEnum;
import cn.example.base.demo.enums.AfterSalesTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author 11030
 */
@Data
@TableName("after_sales")
public class AfterSales implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("service_number")
    private String serviceNumber;

    @TableField("order_id")
    private Long orderId;

    @TableField("order_item_id")
    private Long orderItemId;

    @TableField("user_id")
    private Long userId;

    @TableField("type")
    private Integer type;  // 售后类型: 1-退货,2-换货,3-维修,4-补发

    @TableField("reason")
    private String reason;  // 售后原因

    @TableField("status")
    private Integer status;  // 状态: 0-待处理,1-处理中,2-已完成,3-已关闭

    @TableField("refund_amount")
    private BigDecimal refundAmount;  // 退款金额

    @TableField("solution")
    private String solution;  // 解决方案

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 售后类型文本
    public String getTypeText() {
        return AfterSalesTypeEnum.getDetailText(type);
    }

    // 售后状态文本
    public String getStatusText() {
        return AfterSalesStatusEnum.getText(status);
    }

}
