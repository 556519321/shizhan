package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/test")
@RestController
public class TestPageController {

    //注入freemarker配置对象
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;

    //静态页面放置的路径
    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;


    /**
     * 模拟批量生成静态页面
     * @param goodsIds 商品spu id数组
     * @return 标识符
     */
    @GetMapping("/audit")
    public String auditGoods(Long[] goodsIds) {
        try {
            if (goodsIds != null && goodsIds.length > 0) {
                for (Long goodsId : goodsIds) {
                    genHtml(goodsId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 模拟批量删除静态页面
     * @param goodsIds 商品spu id数组
     * @return 标识符
     */
    @GetMapping("/delete")
    public String deleteGoods(Long[] goodsIds) {
        try {
            if (goodsIds != null && goodsIds.length > 0) {
                for (Long goodsId : goodsIds) {
                    //输出路径
                    String fileName = ITEM_HTML_PATH + goodsId + ".html";
                    File file = new File(fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    private void genHtml(Long goodsId) throws Exception {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //模板
        Template template = configuration.getTemplate("item.ftl");
        //数据
        Map<String, Object> dataModel = new HashMap<>();
        
        //根据商品spu id查询商品基本、描述、sku列表(已启用并按照默认字段降序排序)
        Goods goods = goodsService.findGoodsByIdAndStatus(goodsId, "1");

        //goods 商品基本信息
        dataModel.put("goods", goods.getGoods());
        //goodsDesc 商品描述信息
        dataModel.put("goodsDesc", goods.getGoodsDesc());
        //itemList 商品sku列表；（已启用；需要根据是否默认排序，将默认的sku排在列表中第一个）
        dataModel.put("itemList", goods.getItemList());
        //itemCat1 商品第1级商品分类中文名称
        TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
        dataModel.put("itemCat1", itemCat1.getName());
        //itemCat2 商品第2级商品分类中文名称
        TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
        dataModel.put("itemCat2", itemCat2.getName());
        //itemCat3 商品第3级商品分类中文名称
        TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
        dataModel.put("itemCat3", itemCat3.getName());

        //输出路径
        String fileName = ITEM_HTML_PATH + goodsId + ".html";
        FileWriter fileWriter = new FileWriter(fileName);

        template.process(dataModel, fileWriter);
        fileWriter.close();
    }
}
