package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface SellerService extends BaseService<TbSeller> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param seller 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSeller> search(Integer pageNum, Integer pageSize, TbSeller seller);

}
