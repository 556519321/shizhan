package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/itemCat")
@RestController
public class ItemCatController {

    @Reference
    private ItemCatService itemCatService;

    /**
     * 新增
     * @param itemCat 实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbItemCat itemCat){
        try {
            itemCatService.add(itemCat);

            return Result.ok("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    /**
     * 根据主键查询
     * @param id 主键
     * @return 实体
     */
    @GetMapping("/findOne/{id}")
    public TbItemCat findOne(@PathVariable Long id){
        return itemCatService.findOne(id);
    }

    /**
     * 修改
     * @param itemCat 实体
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbItemCat itemCat){
        try {
            itemCatService.update(itemCat);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     * @return 实体
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            itemCatService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param itemCat 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbItemCat> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbItemCat itemCat) {
        return itemCatService.search(pageNum, pageSize, itemCat);
    }

    @GetMapping("/findAll")
    public List<TbItemCat> findAll(){
        return itemCatService.findAll();
    }

    /**
     * 根据商品分类父分类id查询其对应的所有子分类列表
     * @param parentId 父分类id
     * @return 分类列表
     */
    @GetMapping("/findByParentId")
    public List<TbItemCat> findByParentId(@RequestParam(name="parentId", defaultValue = "0")Long parentId){
        TbItemCat param = new TbItemCat();
        param.setParentId(parentId);
        return itemCatService.findByWhere(param);
    }

}
