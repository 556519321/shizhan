package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 根据查询条件查询es中的数据
     * @param searchMap 查询条件对象
     * @return 查询结果
     */
    Map<String, Object> search(Map<String, Object> searchMap);

    /**
     * 批量保存sku商品数据到es
     * @param itemList 商品sku列表
     */
    void importItemList(List<TbItem> itemList);

    /**
     * 根据spu id数组删除在es中的sku数据
     * @param ids spu id数组
     */
    void deleteItemByIds(Long[] ids);
}
