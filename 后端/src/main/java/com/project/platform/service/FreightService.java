package com.project.platform.service;
import com.project.platform.entity.*;
import com.project.platform.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FreightService {
    @Resource FreightTemplateMapper mapper;
    public List<FreightTemplate> list(Integer merchantId){return mapper.list(merchantId);}
    public void add(FreightTemplate t){mapper.insert(t);}
    public void update(FreightTemplate t){mapper.update(t);}
    public void del(Integer id){mapper.delete(id);}
}