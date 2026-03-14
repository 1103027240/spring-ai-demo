package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单号查询订单（无结果集映射，用于快速查询）
     */
    @Select("SELECT * FROM `order` WHERE order_number = #{orderNumber}")
    Order getByOrderId(@Param("orderNumber") String orderNumber);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM `order` WHERE order_number = #{orderNumber}")
    @Results({
            @Result(property = "id", column = "id", jdbcType = JdbcType.BIGINT),
            @Result(property = "orderNumber", column = "order_number", jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", jdbcType = JdbcType.BIGINT),
            @Result(property = "userName", column = "user_name", jdbcType = JdbcType.VARCHAR),
            @Result(property = "totalAmount", column = "total_amount", jdbcType = JdbcType.DECIMAL),
            @Result(property = "discountAmount", column = "discount_amount", jdbcType = JdbcType.DECIMAL),
            @Result(property = "actualAmount", column = "actual_amount", jdbcType = JdbcType.DECIMAL),
            @Result(property = "status", column = "status", jdbcType = JdbcType.INTEGER),
            @Result(property = "paymentMethod", column = "payment_method", jdbcType = JdbcType.VARCHAR),
            @Result(property = "paymentTime", column = "payment_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "shippingAddress", column = "shipping_address", jdbcType = JdbcType.VARCHAR),
            @Result(property = "contactPhone", column = "contact_phone", jdbcType = JdbcType.VARCHAR),
            @Result(property = "logisticsCompany", column = "logistics_company", jdbcType = JdbcType.VARCHAR),
            @Result(property = "trackingNumber", column = "tracking_number", jdbcType = JdbcType.VARCHAR),
            @Result(property = "shippingTime", column = "shipping_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "receiveTime", column = "receive_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "orderItems", column = "order_items", jdbcType = JdbcType.VARCHAR),
            @Result(property = "remark", column = "remark", jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updateTime", column = "update_time", jdbcType = JdbcType.TIMESTAMP)
    })
    Order selectByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 查询用户最近的订单
     */
    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "orderNumber", column = "order_number"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "totalAmount", column = "total_amount"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Order> selectRecentOrdersByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 根据商品关键词查询订单
     * 说明：订单商品信息存储在 order_items 字段（JSON格式）中，需要解析JSON进行匹配
     */
    @Select("""
            <script>
                SELECT DISTINCT o.* 
                FROM `order` o
                WHERE 1 = 1 
                <if test="userId != null">
                    AND o.user_id = #{userId}
                </if>
                <if test="productKeyword != null and productKeyword != ''">
                    AND (
                        -- 在 order_items 的 JSON 数组中匹配商品名称
                        EXISTS (
                            SELECT 1 
                            FROM JSON_TABLE(o.order_items, '$[*]' COLUMNS (
                                product_name VARCHAR(200) PATH '$.productName',
                                product_code VARCHAR(50) PATH '$.productCode',
                                sku VARCHAR(50) PATH '$.sku'
                            )) AS items
                            WHERE items.product_name LIKE CONCAT('%', #{productKeyword}, '%')
                               OR items.product_code LIKE CONCAT('%', #{productKeyword}, '%')
                               OR items.sku LIKE CONCAT('%', #{productKeyword}, '%')
                        )
                        -- 或者备注中包含关键词
                        OR o.remark LIKE CONCAT('%', #{productKeyword}, '%')
                    )
                </if>
                <if test="startTime != null">
                    AND o.create_time >= #{startTime}
                </if>
                <if test="endTime != null">
                    AND o.create_time &lt;= #{endTime}
                </if>
                <if test="status != null">
                    AND o.status = #{status}
                </if>
                 <choose>
                    <when test="orderBy != null and orderBy != ''">
                         ORDER BY ${orderBy}
                         <choose>
                             <when test = "orderDesc != null and orderDesc != ''">
                                ${orderDesc}
                             </when>
                             <otherwise>
                                DESC
                             </otherwise>
                         </choose>
                    </when>
                    <otherwise>
                        ORDER BY create_time DESC
                    </otherwise>
                </choose>
                <if test="limit != null and limit > 0">
                    LIMIT #{limit}
                </if>
            </script>
    """)
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "orderNumber", column = "order_number"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "totalAmount", column = "total_amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Order> selectByProductKeyword(Map<String, Object> params);

    /**
     * 根据状态和时间范围查询订单
     */
    @Select("""
            <script>
                SELECT * FROM `order`
                WHERE 1 = 1
                <if test="userId != null">
                    AND user_id = #{userId}
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
                <if test="startTime != null">
                    AND create_time >= #{startTime}
                </if>
                <if test="endTime != null">
                    AND create_time &lt;= #{endTime}
                </if>
                 <choose>
                    <when test="orderBy != null and orderBy != ''">
                         ORDER BY ${orderBy}
                         <choose>
                             <when test = "orderDesc != null and orderDesc != ''">
                                ${orderDesc}
                             </when>
                             <otherwise>
                                DESC
                             </otherwise>
                         </choose>
                    </when>
                    <otherwise>
                        ORDER BY create_time DESC
                    </otherwise>
                </choose>
                <if test="limit != null and limit > 0">
                    LIMIT #{limit}
                </if>
            </script>
    """)
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "orderNumber", column = "order_number"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "totalAmount", column = "total_amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Order> selectByStatusAndTimeRange(Map<String, Object> params);

    /**
     * 根据时间范围查询订单
     */
    @Select("""
            <script>
                SELECT * FROM `order`
                WHERE 1 = 1
                <if test="userId != null">
                    AND user_id = #{userId}
                </if>
                <if test="startTime != null">
                    AND create_time >= #{startTime}
                </if>
                <if test="endTime != null">
                    AND create_time &lt;= #{endTime}
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
                <choose>
                    <when test="orderBy != null and orderBy != ''">
                         ORDER BY ${orderBy}
                         <choose>
                             <when test = "orderDesc != null and orderDesc != ''">
                                ${orderDesc}
                             </when>
                             <otherwise>
                                DESC
                             </otherwise>
                         </choose>
                    </when>
                    <otherwise>
                        ORDER BY create_time DESC
                    </otherwise>
                </choose>
                <if test="limit != null and limit > 0">
                    LIMIT #{limit}
                </if>   
            </script>
    """)
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "orderNumber", column = "order_number"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "totalAmount", column = "total_amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Order> selectByTimeRange(Map<String, Object> params);

}
