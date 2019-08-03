package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface SpecificationOptionService extends BaseService<TbSpecificationOption> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param specificationOption 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSpecificationOption> search(Integer pageNum, Integer pageSize, TbSpecificationOption specificationOption);

}
