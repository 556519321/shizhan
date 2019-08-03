package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private SellerMapper sellerMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;

    @Override
    public PageInfo<TbGoods> search(Integer pageNum, Integer pageSize, TbGoods goods) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbGoods.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //要查询非删除状态的商品数据
        criteria.andNotEqualTo("isDelete", "1");

        //审核状态
        if (StringUtils.isNotBlank(goods.getAuditStatus())) {
            criteria.andEqualTo("auditStatus", goods.getAuditStatus());
        }
        //商家
        if (StringUtils.isNotBlank(goods.getSellerId())) {
            criteria.andEqualTo("sellerId", goods.getSellerId());
        }

        //商品名称模糊查询
        if (StringUtils.isNotBlank(goods.getGoodsName())) {
            criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
        }

        List<TbGoods> list = goodsMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addGoods(Goods goods) {
        //保存商品基本信息
        //新增商品；状态应该为未审核
        goods.getGoods().setAuditStatus("0");
        add(goods.getGoods());

        //int i = 1/0;

        //保存商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());

        goodsDescMapper.insertSelective(goods.getGoodsDesc());

        //保存商品sku列表信息
        saveItemList(goods);
    }

    @Override
    public Goods findGoodsById(Long id) {
        return findGoodsByIdAndStatus(id, null);
    }

    @Override
    public void updateGoods(Goods goods) {
        //更新基本信息
        //只要修改了商品都需要重新审核；未审核
        goods.getGoods().setAuditStatus("0");
        update(goods.getGoods());

        //更新描述信息
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());
        //更新sku信息

        //删除该spu对应的所有sku
        //delete from tb_item where goods_id=?
        TbItem item = new TbItem();
        item.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(item);

        saveItemList(goods);
    }

    @Override
    public void updateStatus(String status, Long[] ids) {
        //更新商品spu的审核状态
        /**
         * update tb_goods
         * set audit_status='1'
         * where id in(?,?...)
         */
        TbGoods tbGoods = new TbGoods();
        tbGoods.setAuditStatus(status);

        //更新条件
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));

        goodsMapper.updateByExampleSelective(tbGoods, example);

        /**
         * -- 如果审核通过商品spu的话；那么需要将所有这些spu对应所有sku的状态修改为启用
         * update tb_item
         * set status='1'
         * where goods_id in(?,?,...)
         */

        TbItem item = new TbItem();
        item.setStatus("0");

        if ("2".equals(status)) {
            //已启用
            item.setStatus("1");
        }
        //更新条件
        Example itemExample = new Example(TbItem.class);
        itemExample.createCriteria().andIn("goodsId", Arrays.asList(ids));

        itemMapper.updateByExampleSelective(item, itemExample);

    }

    @Override
    public void deleteGoodsByIds(Long[] ids) {
        //批量地将商品spu的删除状态修改为1
        //update tb_goods set is_delete='1' where id in(?,?....)
        TbGoods tbGoods = new TbGoods();
        tbGoods.setIsDelete("1");

        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));

        goodsMapper.updateByExampleSelective(tbGoods, example);
    }

    @Override
    public List<TbItem> findItemListByGoodsIds(Long[] ids) {
        //select * from tb_item where status='1' and goods_id in(?,?...)
        Example example = new Example(TbItem.class);
        example.createCriteria()
                .andEqualTo("status", "1")
                .andIn("goodsId", Arrays.asList(ids));
        return itemMapper.selectByExample(example);
    }

    @Override
    public Goods findGoodsByIdAndStatus(Long goodsId, String itemStatus) {
        Goods goods = new Goods();

        //商品基本信息
        goods.setGoods(findOne(goodsId));

        //商品描述信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(goodsId));

        //商品sku信息
        //sql ==> select * from tb_item where goods_id=? and status='1' order by is_default desc
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("goodsId", goodsId);
        if (itemStatus != null) {
            criteria.andEqualTo("status", itemStatus);
        }
        example.orderBy("isDefault").desc();

        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);

        return goods;

    }

    /**
     * 保存商品sku列表信息
     *
     * @param goods 基本、描述、sku列表
     */
    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //启用规格
            if (goods.getItemList() != null && goods.getItemList().size() > 0) {
                for (TbItem tbItem : goods.getItemList()) {

                    //标题
                    String title = goods.getGoods().getGoodsName();
                    //转换规格属性为对象
                    Map<String, String> specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
                    for (Map.Entry<String, String> entry : specMap.entrySet()) {
                        title += " " + entry.getValue();
                    }
                    tbItem.setTitle(title);

                    //设置item的值
                    setItemValue(goods, tbItem);

                    //保存sku
                    itemMapper.insertSelective(tbItem);
                }
            }
        } else {
            //不启用规格
            TbItem tbItem = new TbItem();

            tbItem.setTitle(goods.getGoods().getGoodsName());

            //是否启用：不启用，0
            tbItem.setStatus("0");
            //是否默认：默认，1
            tbItem.setIsDefault("1");
            //库存：9999
            tbItem.setNum(9999);
            //价格：按照spu的价格
            tbItem.setPrice(goods.getGoods().getPrice());
            //规格spec：{}
            tbItem.setSpec("{}");
            //其它数据的来源与原来的做法一样的代码。

            //设置item的值
            setItemValue(goods, tbItem);

            itemMapper.insertSelective(tbItem);
        }
    }

    private void setItemValue(Goods goods, TbItem tbItem) {
        //图片获取spu的第一张图片
        if (StringUtils.isNotBlank(goods.getGoodsDesc().getItemImages())) {
            List<Map> images = JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            if (images != null && images.size() > 0) {
                tbItem.setImage(images.get(0).get("url") + "");
            }
        }

        //商家
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        tbItem.setSellerId(seller.getSellerId());
        tbItem.setSeller(seller.getName());

        //品牌
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        tbItem.setBrand(brand.getName());

        //商品分类
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        tbItem.setCategoryid(itemCat.getId());
        tbItem.setCategory(itemCat.getName());

        tbItem.setCreateTime(new Date());
        tbItem.setUpdateTime(tbItem.getCreateTime());

        tbItem.setGoodsId(goods.getGoods().getId());
    }

}
