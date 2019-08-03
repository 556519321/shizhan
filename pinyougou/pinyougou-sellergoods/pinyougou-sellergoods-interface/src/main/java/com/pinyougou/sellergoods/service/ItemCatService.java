package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface ItemCatService extends BaseService<TbItemCat> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param itemCat 搜索条件
     * @return 分页信息
     */
    PageInfo<TbItemCat> search(Integer pageNum, Integer pageSize, TbItemCat itemCat);

}
