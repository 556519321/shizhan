package com.pinyougou.order.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface OrderItemService extends BaseService<TbOrderItem> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param orderItem 搜索条件
     * @return 分页信息
     */
    PageInfo<TbOrderItem> search(Integer pageNum, Integer pageSize, TbOrderItem orderItem);

}
