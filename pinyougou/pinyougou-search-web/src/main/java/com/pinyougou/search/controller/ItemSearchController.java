package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/itemSearch")
@RestController
public class ItemSearchController {

    @Reference
    private ItemSearchService itemSearchService;

    /**
     * 根据查询条件查询es中的数据
     * @param searchMap 查询条件对象
     * @return 查询结果
     */
    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody Map<String, Object> searchMap){
        return itemSearchService.search(searchMap);
    }
}
