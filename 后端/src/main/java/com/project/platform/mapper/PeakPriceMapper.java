package com.project.platform.mapper;
import com.project.platform.entity.PeakPrice;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PeakPriceMapper {
    List<PeakPrice> list(Integer merchantId);
    void insert(PeakPrice p);
    void update(PeakPrice p);
    void delete(Integer id);
}