package com.pinyougou.mapper;

import com.pinyougou.pojo.TbSpecification;

import java.util.List;
import java.util.Map;

public interface SpecificationMapper extends BaseMapper<TbSpecification> {
    List<Map<String, Object>> selectOptionList();
}
