package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandMapper extends BaseMapper<TbBrand> {
    List<TbBrand> queryAll();

    List<Map<String, Object>> selectOptionList();
}
