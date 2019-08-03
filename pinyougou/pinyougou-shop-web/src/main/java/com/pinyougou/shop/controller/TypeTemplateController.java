package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/typeTemplate")
@RestController
public class TypeTemplateController {

    //设置超时时间，默认1秒，现在设置了100秒
    @Reference(timeout = 100000)
    private TypeTemplateService typeTemplateService;

    /**
     * 新增
     * @param typeTemplate 实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbTypeTemplate typeTemplate){
        try {
            typeTemplateService.add(typeTemplate);

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
    public TbTypeTemplate findOne(@PathVariable Long id){
        return typeTemplateService.findOne(id);
    }

    /**
     * 修改
     * @param typeTemplate 实体
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbTypeTemplate typeTemplate){
        try {
            typeTemplateService.update(typeTemplate);
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
            typeTemplateService.deleteByIds(ids);
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
     * @param typeTemplate 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbTypeTemplate> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbTypeTemplate typeTemplate) {
        return typeTemplateService.search(pageNum, pageSize, typeTemplate);
    }

    /**
     * 根据分类模板id 查询规格及其选项
     * [{"id":1,"specName":"机身内存","options":[{"id":1,"optionName":"16G"},{"id":1,"optionName":"32G"}]},...,{}]
     * @param id 分类模板id
     * @return 规格及其选项
     */
    @GetMapping("/findSpecList")
    public List<Map> findSpecList(Long id){
        return typeTemplateService.findSpecList(id);
    }

}
