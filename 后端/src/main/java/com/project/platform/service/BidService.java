package com.project.platform.service;
import com.project.platform.entity.*;
import com.project.platform.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class BidService{
    @Resource BidStrategyMapper mapper;
    public List<BidStrategy> list(Integer merchantId){return mapper.list(merchantId);}
    public void add(BidStrategy b){mapper.insert(b);}
    public void update(BidStrategy b){mapper.update(b);}
    public void del(Integer id){mapper.delete(id);}
}
