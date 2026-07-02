package com.project.platform.service;

import com.project.platform.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> list(Integer userId);
    void add(Address address);
    Address getById(Integer id);
    void update(Address address);
    void delete(Integer id);
}