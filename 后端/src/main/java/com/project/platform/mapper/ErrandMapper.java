package com.project.platform.mapper;

import com.project.platform.entity.Errand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ErrandMapper {

    @Insert("INSERT INTO errand(order_id, user_id, delivery_id, errand_type, start_address, end_address, remark, status) " +
            "VALUES(#{orderId}, #{userId}, #{deliveryId}, #{errandType}, #{startAddress}, #{endAddress}, #{remark}, #{status})")
    int insert(Errand errand);

    @Select("SELECT * FROM errand WHERE order_id = #{orderId}")
    Errand selectByOrderId(String orderId);
    @Update("UPDATE errand SET delivery_id=#{deliveryId}, status=#{status} WHERE errand_id=#{errandId}")
    int update(Errand errand);
}