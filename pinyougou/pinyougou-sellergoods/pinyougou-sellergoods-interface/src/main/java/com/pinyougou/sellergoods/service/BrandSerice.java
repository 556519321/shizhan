package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BaseService;

import java.util.List;
import java.util.Map;

public interface BrandSerice extends BaseService<TbBrand> {
    List<TbBrand> queryAll();

    /**
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 品牌列表json格式字符串
     */
    List<TbBrand> testPage(Integer pageNum, Integer pageSize);

    /**
     * 条件模糊分页查询
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param brand 查询条件对象
     * @return 分页信息对象
     */
    PageInfo<TbBrand> search(Integer pageNum, Integer pageSize, TbBrand brand);

    /**
     * 获取品牌下拉框数据，数据如返回结果
     * @return [{id:'1',text:'联想'},{id:'2',text:'华为'}]
     */
    List<Map<String, Object>> selectOptionList();
}
