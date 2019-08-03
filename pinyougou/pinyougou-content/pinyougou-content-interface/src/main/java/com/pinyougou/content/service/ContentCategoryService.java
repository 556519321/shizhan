package com.pinyougou.content.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbContentCategory;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface ContentCategoryService extends BaseService<TbContentCategory> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param contentCategory 搜索条件
     * @return 分页信息
     */
    PageInfo<TbContentCategory> search(Integer pageNum, Integer pageSize, TbContentCategory contentCategory);

}
