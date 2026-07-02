package com.project.platform.service.impl;

import com.project.platform.entity.Address;
import com.project.platform.mapper.AddressMapper;
import com.project.platform.service.AddressService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Resource
    private AddressMapper addressMapper;

    @Override
    public List<Address> list(Integer userId) {
        return addressMapper.list(userId);
    }

    @Override
    @Transactional
    public void add(Address address) {
        if (address.getIsDefault() == 1) {
            addressMapper.cancelDefault(address.getUserId());
        }
        addressMapper.insert(address);
    }

    @Override
    public Address getById(Integer id) {
        return addressMapper.selectById(id);
    }

    @Override
    @Transactional
    public void update(Address address) {
        if (address.getIsDefault() == 1) {
            addressMapper.cancelDefault(address.getUserId());
        }
        addressMapper.updateById(address);
    }

    @Override
    public void delete(Integer id) {
        addressMapper.deleteById(id);
    }
}