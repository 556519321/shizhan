package com.pinyougou.mapper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/*.xml")
public class BrandMapperTest {

    @Autowired
    private BrandMapper brandMapper;


    /**
     * 新增
     * insert into tb_brand(id, name, first_char) values(?,?,?)
     * 选择性新增;在TbBrand实例中firstChar的值为null
     * insert into tb_brand(id, name) values(?,?)
     */
    @Test
    public void insertSelective() {
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("test1");
        tbBrand.setFirstChar("T");
        brandMapper.insertSelective(tbBrand);
    }

    /**
     * 查询所有
     */
    @Test
    public void selectAll() {
        List<TbBrand> list = brandMapper.selectAll();
        for (TbBrand tbBrand : list) {
            System.out.println(tbBrand);
        }
    }

    /**
     * 根据主键查询
     */
    @Test
    public void selectByPrimaryKey() {
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(1L);
        System.out.println(tbBrand);
    }

    /**
     * 条件查询
     */
    @Test
    public void selectByWhere() {
        TbBrand param = new TbBrand();
        param.setFirstChar("H");
        List<TbBrand> list = brandMapper.select(param);
        for (TbBrand tbBrand : list) {
            System.out.println(tbBrand);
        }
    }

    /**
     * 选择性根据主键更新
     */
    @Test
    public void updateByPrimaryKeySelective() {
        TbBrand brand = new TbBrand();
        brand.setId(23L);
        brand.setName("i hate jbl");

        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 根据主键删除
     */
    @Test
    public void deleteByPrimaryKey() {
        brandMapper.deleteByPrimaryKey(23L);
    }

    /**
     * 根据条件动态查询（极其复杂的条件）
     */
    @Test
    public void selectByExample() {
        //分页设置
        //参数1：页号，参数2：页大小
        PageHelper.startPage(2, 2);

        //创建查询对象
        Example example = new Example(TbBrand.class);

        //创建条件对象
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("firstChar", "C");

        List<TbBrand> list = brandMapper.selectByExample(example);

        //转换分页信息对象
        PageInfo<TbBrand> pageInfo = new PageInfo<>(list);

        System.out.println("总记录数为：" + pageInfo.getTotal());
        System.out.println("总页数为：" + pageInfo.getPages());
        System.out.println("当前页为：" + pageInfo.getPageNum());
        System.out.println("页大小为：" + pageInfo.getPageSize());

        for (TbBrand tbBrand : pageInfo.getList()) {
            System.out.println(tbBrand);
        }

    }

    //批量新增
    @Test
    public void insertList(){
        List<TbBrand> list = new ArrayList<>();
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("test1");
        tbBrand.setFirstChar("T");
        list.add(tbBrand);

        tbBrand = new TbBrand();
        tbBrand.setName("test2");
        tbBrand.setFirstChar("T");
        list.add(tbBrand);

        brandMapper.insertList(list);
    }

    //批量删除
    @Test
    public void deleteByIds(){
        Long[] ids = {26L, 27L};
        //StringUtils.join(ids, ",") ====> 24,25
        brandMapper.deleteByIds(StringUtils.join(ids, ","));
    }

}