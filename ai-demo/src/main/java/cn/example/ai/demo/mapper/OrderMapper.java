package cn.example.ai.demo.mapper;

import cn.example.ai.demo.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM `order` WHERE order_number = #{orderNumber}")
    Order getByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 查询用户最近的订单
     */
    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Order> selectRecentOrdersByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 根据商品关键词查询订单
     * 支持在 order_items JSON 字段中匹配商品信息
     */
    @Select("""
            <script>
                SELECT o.* 
                FROM `order` o
                WHERE 1 = 1
                <if test="params.userId != null">
                    AND o.user_id = #{params.userId}
                </if>
                <if test="params.productKeyword != null and params.productKeyword != ''">
                    AND (
                        -- 在 order_items 的 JSON 数组中匹配商品名称
                        EXISTS (
                            SELECT 1 
                            FROM JSON_TABLE(o.order_items, '$[*]' COLUMNS (
                                product_name VARCHAR(200) PATH '$.productName',
                                product_code VARCHAR(50) PATH '$.productCode',
                                sku VARCHAR(50) PATH '$.sku'
                            )) AS items
                            WHERE items.product_name LIKE CONCAT('%', #{params.productKeyword}, '%')
                               OR items.product_code LIKE CONCAT('%', #{params.productKeyword}, '%')
                               OR items.sku LIKE CONCAT('%', #{params.productKeyword}, '%')
                        )
                        -- 或者备注中包含关键词
                        OR o.remark LIKE CONCAT('%', #{params.productKeyword}, '%')
                    )
                </if>
                <if test="params.status != null">
                    AND o.status = #{params.status}
                </if>
                <if test="params.startTime != null">
                    AND o.create_time >= #{params.startTime}
                </if>
                <if test="params.endTime != null">
                    AND o.create_time &lt;= #{params.endTime}
                </if>
                <choose>
                    <when test="params.orderBy != null and params.orderBy != ''">
                         ORDER BY ${params.orderBy}
                         <choose>
                             <when test = "params.orderDesc != null and params.orderDesc != ''">
                                ${params.orderDesc}
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
                <if test="params.limit != null and params.limit > 0">
                    LIMIT #{params.limit}
                </if>
            </script>
        """)
    List<Order> selectByProductKeyword(@Param("params") Map<String, Object> params);

    /**
     * 根据状态和时间范围查询订单
     */
    @Select("""
            <script>
                SELECT * FROM `order`
                WHERE 1 = 1
                <if test="params.userId != null">
                    AND user_id = #{params.userId}
                </if>
                <if test="params.status != null">
                    AND status = #{params.status}
                </if>
                <if test="params.startTime != null">
                    AND create_time >= #{params.startTime}
                </if>
                <if test="params.endTime != null">
                    AND create_time &lt;= #{params.endTime}
                </if>
                <choose>
                    <when test="params.orderBy != null and params.orderBy != ''">
                         ORDER BY ${params.orderBy}
                         <choose>
                             <when test = "params.orderDesc != null and params.orderDesc != ''">
                                ${params.orderDesc}
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
                <if test="params.limit != null and params.limit > 0">
                    LIMIT #{params.limit}
                </if>
            </script>
        """)
    List<Order> selectByStatusAndTimeRange(@Param("params") Map<String, Object> params);

    /**
     * 根据时间范围查询订单
     */
    @Select("""
            <script>
                SELECT * FROM `order`
                WHERE 1 = 1
                <if test="params.userId != null">
                    AND user_id = #{params.userId}
                </if>
                <if test="params.startTime != null">
                    AND create_time >= #{params.startTime}
                </if>
                <if test="params.endTime != null">
                    AND create_time &lt;= #{params.endTime}
                </if>
                <if test="params.status != null">
                    AND status = #{params.status}
                </if>
                <choose>
                    <when test="params.orderBy != null and params.orderBy != ''">
                         ORDER BY ${params.orderBy}
                         <choose>
                             <when test = "params.orderDesc != null and params.orderDesc != ''">
                                ${params.orderDesc}
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
                <if test="params.limit != null and params.limit > 0">
                    LIMIT #{params.limit}
                </if>
            </script>
        """)
    List<Order> selectByTimeRange(@Param("params") Map<String, Object> params);

    /**
     * 通用条件查询
     * 支持多条件组合查询
     */
    @Select("""
            <script>
                SELECT * FROM `order`
                WHERE 1 = 1
                <if test="params.userId != null">
                    AND user_id = #{params.userId}
                </if>
                <if test="params.orderNumber != null and params.orderNumber != ''">
                    AND order_number = #{params.orderNumber}
                </if>
                <if test="params.status != null">
                    AND status = #{params.status}
                </if>
                <if test="params.productKeyword != null and params.productKeyword != ''">
                    AND (
                        -- 在 order_items 的 JSON 数组中匹配商品名称
                        EXISTS (
                            SELECT 1 
                            FROM JSON_TABLE(o.order_items, '$[*]' COLUMNS (
                                product_name VARCHAR(200) PATH '$.productName',
                                product_code VARCHAR(50) PATH '$.productCode',
                                sku VARCHAR(50) PATH '$.sku'
                            )) AS items
                            WHERE items.product_name LIKE CONCAT('%', #{params.productKeyword}, '%')
                               OR items.product_code LIKE CONCAT('%', #{params.productKeyword}, '%')
                               OR items.sku LIKE CONCAT('%', #{params.productKeyword}, '%')
                        )
                        -- 或者备注中包含关键词
                        OR o.remark LIKE CONCAT('%', #{params.productKeyword}, '%')
                    )
                </if>
                <if test="params.startTime != null">
                    AND create_time >= #{params.startTime}
                </if>
                <if test="params.endTime != null">
                    AND create_time &lt;= #{params.endTime}
                </if>
                <choose>
                    <when test="params.orderBy != null and params.orderBy != ''">
                         ORDER BY ${params.orderBy}
                         <choose>
                             <when test = "params.orderDesc != null and params.orderDesc != ''">
                                ${params.orderDesc}
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
                <if test="params.limit != null and params.limit > 0">
                    LIMIT #{params.limit}
                </if>
            </script>
        """)
    List<Order> selectByCondition(@Param("params") Map<String, Object> params);

}