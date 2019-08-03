package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandSerice;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/brand")
@RestController
public class BrandController {

    @Reference
    private BrandSerice brandSerice;

    /**
     * 根据id查询品牌
     * @param id 品牌id
     * @return 品牌
     */
    @GetMapping("/findOne/{id}")
    public TbBrand findOne(@PathVariable Long id){
        return brandSerice.findOne(id);
    }

    /**
     * 新增品牌
     * @param brand 品牌信息对象
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody  TbBrand brand){
        try {
            brandSerice.add(brand);

            //return new Result(true, "新增品牌成功");
            return Result.ok("新增品牌成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return new Result(false, "新增品牌失败");
        return Result.fail("新增品牌失败");
    }

    /**
     * 更新品牌
     * @param brand 品牌信息对象
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody  TbBrand brand){
        try {
            brandSerice.update(brand);

            return Result.ok("修改品牌成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改品牌失败");
    }

    /**
     * 批量删品牌
     * @param ids 品牌id数组
     * @return 操作结果
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandSerice.deleteByIds(ids);
            return Result.ok("删除品牌成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除品牌失败");
    }

    /**
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 品牌列表json格式字符串
     */
    @GetMapping("/findPage")
    public PageInfo<TbBrand> findPage(@RequestParam(name="pageNum", defaultValue = "1")Integer pageNum,
                             @RequestParam(name="pageSize", defaultValue = "10")Integer pageSize){
        return brandSerice.findPage(pageNum, pageSize);
    }
    /**
     * http://localhost:9100/brand/testPage.do?pageNum=1&pageSize=5
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 品牌列表json格式字符串
     */
    @GetMapping("/testPage")
    public List<TbBrand> testPage(@RequestParam(name="pageNum", defaultValue = "1")Integer pageNum,
                                  @RequestParam(name="pageSize", defaultValue = "10")Integer pageSize){
        //return brandSerice.testPage(pageNum, pageSize);
        return brandSerice.findPage(pageNum, pageSize).getList();
    }

    //查询所有数据
    @GetMapping("/findAll")
    public List<TbBrand> findAll(){
        //return brandSerice.queryAll();
        return brandSerice.findAll();
    }

    /**
     * 条件模糊分页查询
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param brand 查询条件对象
     * @return 分页信息对象
     */
    @PostMapping("/search")
    public PageInfo<TbBrand> search(@RequestParam(name="pageNum", defaultValue = "1")Integer pageNum,
                                    @RequestParam(name="pageSize", defaultValue = "10")Integer pageSize,
                                    @RequestBody TbBrand brand){
        return brandSerice.search(pageNum, pageSize, brand);
    }

    /**
     * 获取品牌下拉框数据，数据如返回结果
     * @return [{id:'1',text:'联想'},{id:'2',text:'华为'}]
     */
    @GetMapping("/selectOptionList")
    public List<Map<String, Object>> selectOptionList(){
        return brandSerice.selectOptionList();
    }
}
