package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandSerice;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class BrandSericeImpl extends BaseServiceImpl<TbBrand> implements BrandSerice {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer pageNum, Integer pageSize) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);

        return brandMapper.selectAll();
    }

    @Override
    public PageInfo<TbBrand> search(Integer pageNum, Integer pageSize, TbBrand brand) {
        //分页
        PageHelper.startPage(pageNum, pageSize);

        //sql ：select * from tb_brand where name like '%?%' and first_char = ?
        //设置条件并查询
        Example example = new Example(TbBrand.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //根据首字母查询
        if (StringUtils.isNotBlank(brand.getFirstChar())) {
            criteria.andEqualTo("firstChar", brand.getFirstChar());
        }
        //根据品牌名称模糊查询
        if (StringUtils.isNotBlank(brand.getName())) {
            criteria.andLike("name", "%"+brand.getName()+"%");
        }

        //根据首字母排序
        //example.orderBy("firstChar").desc();

        List<TbBrand> list = brandMapper.selectByExample(example);

        //返回分页对象
        return new PageInfo<>(list);
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
