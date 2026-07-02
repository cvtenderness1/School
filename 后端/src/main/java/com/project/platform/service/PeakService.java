package com.project.platform.service;
import com.project.platform.entity.*;
import com.project.platform.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class PeakService{
    @Resource PeakPriceMapper mapper;
    public List<PeakPrice> list(Integer merchantId){return mapper.list(merchantId);}
    public void add(PeakPrice p){mapper.insert(p);}
    public void update(PeakPrice p){mapper.update(p);}
    public void del(Integer id){mapper.delete(id);}
}
