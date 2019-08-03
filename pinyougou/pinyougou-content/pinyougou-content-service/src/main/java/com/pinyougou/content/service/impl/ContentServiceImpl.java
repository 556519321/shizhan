package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //内容缓存在redis中的key的名称
    private static final String REDIS_CONTENT = "CONTENT_LIST";

    @Override
    public void add(TbContent tbContent) {
        super.add(tbContent);
        //将新增内容对应的内容分类在redis中的缓冲删除
        updateContentListInRedisByCategoryId(tbContent.getCategoryId());
    }

    /**
     * 将新增内容对应的内容分类在redis中的缓冲删除
     * @param categoryId 内容分类id
     */
    private void updateContentListInRedisByCategoryId(Long categoryId) {
        redisTemplate.boundHashOps(REDIS_CONTENT).delete(categoryId);
    }

    @Override
    public void update(TbContent tbContent) {
        //如果对内容修改了内容分类；则需要将该内容的原有内容分类缓冲删除，并删除新内容分类的缓冲。
        TbContent oldContent = findOne(tbContent.getId());
        if (!oldContent.getCategoryId().equals(tbContent.getCategoryId())) {
            updateContentListInRedisByCategoryId(oldContent.getCategoryId());
        }

        super.update(tbContent);

        //删除新内容中分类的缓冲
        updateContentListInRedisByCategoryId(tbContent.getCategoryId());
    }

    @Override
    public void deleteByIds(Serializable[] ids) {
        //需要根据内容分类id数组查询所有内容列表；再遍历每一个内容，根据其内容分类到redis删除缓冲
        //select * from tb_content where id in(?...)
        Example example = new Example(TbContent.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        List<TbContent> contentList = contentMapper.selectByExample(example);
        for (TbContent tbContent : contentList) {
            updateContentListInRedisByCategoryId(tbContent.getCategoryId());
        }
        super.deleteByIds(ids);
    }

    @Override
    public PageInfo<TbContent> search(Integer pageNum, Integer pageSize, TbContent content) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbContent.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(content.getProperty())) {
            criteria.andLike("property", "%" + content.getProperty() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> contentList = null;

        try {
            //从redis中查询数据；如果有则直接返回
            contentList = (List<TbContent>) redisTemplate.boundHashOps(REDIS_CONTENT).get(categoryId);
            if (contentList != null && contentList.size() > 0) {
                return contentList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //-- 查询首页轮播广告 分类、有效的并且根据排序字段降序排序的内容数据
        //select * from tb_content where category_id = ? and status='1' order by sort_order desc
        Example example = new Example(TbContent.class);

        example.createCriteria()
                .andEqualTo("status", "1")
                .andEqualTo("categoryId", categoryId);

        //设置排序
        example.orderBy("sortOrder").desc();

        contentList = contentMapper.selectByExample(example);

        try {
            //设置内容列表到redis中
            redisTemplate.boundHashOps(REDIS_CONTENT).put(categoryId, contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentList;
    }

}
