package com.project.platform.entity;
import lombok.Data;

@Data
public class Category {
    private Integer categoryId;
    private Integer merchantId;
    private String categoryName;
    private Integer sort;
}