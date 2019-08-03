package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentCategoryMapper;
import com.pinyougou.pojo.TbContentCategory;
import com.pinyougou.content.service.ContentCategoryService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ContentCategoryServiceImpl extends BaseServiceImpl<TbContentCategory> implements ContentCategoryService {

    @Autowired
    private ContentCategoryMapper contentCategoryMapper;

    @Override
    public PageInfo<TbContentCategory> search(Integer pageNum, Integer pageSize, TbContentCategory contentCategory) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbContentCategory.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        if (StringUtils.isNotBlank(contentCategory.getName())) {
            criteria.andLike("name", "%" + contentCategory.getName() + "%");
        }

        List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

}
