package com.project.platform.mapper;
import com.project.platform.entity.FreightTemplate;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FreightTemplateMapper {
    List<FreightTemplate> list(Integer merchantId);
    void insert(FreightTemplate t);
    void update(FreightTemplate t);
    void delete(Integer id);
}