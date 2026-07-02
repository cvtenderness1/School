package com.project.platform.entity;

import lombok.Data;
import java.util.List;

@Data
public class GoodsSpec {
    private Integer id;
    private Integer goodsId;
    private String name;
    private List<GoodsSpecItem> list;
}