package com.pinyougou.user.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface AddressService extends BaseService<TbAddress> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param address 搜索条件
     * @return 分页信息
     */
    PageInfo<TbAddress> search(Integer pageNum, Integer pageSize, TbAddress address);

}
