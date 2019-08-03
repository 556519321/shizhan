package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.order.service.OrderItemService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class OrderItemServiceImpl extends BaseServiceImpl<TbOrderItem> implements OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public PageInfo<TbOrderItem> search(Integer pageNum, Integer pageSize, TbOrderItem orderItem) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbOrderItem.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(orderItem.getProperty())) {
            criteria.andLike("property", "%" + orderItem.getProperty() + "%");
        }*/

        List<TbOrderItem> list = orderItemMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

}
