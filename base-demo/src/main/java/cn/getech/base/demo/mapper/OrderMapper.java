package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * @author 11030
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM order WHERE order_number = #{orderNumber}")
    Order selectByOrderNumber(@Param("orderNumber") String orderNumber);

    @Select("SELECT * FROM order WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Order> selectRecentOrdersByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM order WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM order WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") int status);

    @Select("SELECT * FROM order WHERE order_number = #{orderNumber}")
    Order getByOrderId(String orderNumber);

}
