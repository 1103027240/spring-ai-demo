package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.AfterSales;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author 11030
 */
@Mapper
public interface AfterSalesMapper extends BaseMapper<AfterSales> {

    @Select("SELECT * FROM after_sales WHERE service_number = #{serviceNumber}")
    AfterSales selectByServiceNumber(@Param("serviceNumber") String serviceNumber);
}
