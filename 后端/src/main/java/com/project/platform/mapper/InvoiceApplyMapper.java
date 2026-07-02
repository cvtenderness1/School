package com.project.platform.mapper;

import com.project.platform.entity.InvoiceApply;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface InvoiceApplyMapper {
    int insertInvoice(InvoiceApply apply);
    List<InvoiceApply> selectList(@Param("status") Integer status);
    int updateInvoiceStatus(@Param("id") Long id, @Param("status") Integer status);

    void deleteById(Integer id);
}