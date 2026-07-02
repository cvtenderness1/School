package com.project.platform.mapper;
import com.project.platform.entity.Rider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface RiderMapper {
    List<Rider> list(Rider rider);
    int insert(Rider rider);
    int updateById(Rider rider);
    List<Rider> selectAll();
    boolean removeByIds(List<Integer> ids);
    @Select("SELECT * FROM rider WHERE account = #{account}")
    Rider selectByAccount(String account);

    Rider selectByPhone(String phone);

    Rider selectById(Integer riderId);

    void addBalance(@Param("riderId") Integer riderId,
                    @Param("money") BigDecimal money);

    void updateLocation(@Param("riderId") Integer riderId,
                        @Param("lat") BigDecimal lat,
                        @Param("lng") BigDecimal lng);

    List<Rider> selectBalanceRank();
    void batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);


    BigDecimal selectBalance(@Param("riderId") Integer riderId);

    int withdrawAllBalance(Integer riderId);
}