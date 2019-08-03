package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbContentCategory;
import com.pinyougou.content.service.ContentCategoryService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/contentCategory")
@RestController
public class ContentCategoryController {

    @Reference
    private ContentCategoryService contentCategoryService;

    /**
     * 新增
     * @param contentCategory 实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbContentCategory contentCategory){
        try {
            contentCategoryService.add(contentCategory);

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
    public TbContentCategory findOne(@PathVariable Long id){
        return contentCategoryService.findOne(id);
    }

    /**
     * 修改
     * @param contentCategory 实体
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbContentCategory contentCategory){
        try {
            contentCategoryService.update(contentCategory);
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
            contentCategoryService.deleteByIds(ids);
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
     * @param contentCategory 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbContentCategory> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbContentCategory contentCategory) {
        return contentCategoryService.search(pageNum, pageSize, contentCategory);
    }

    @GetMapping("/findAll")
    public List<TbContentCategory> findAll(){
        return contentCategoryService.findAll();
    }

}
