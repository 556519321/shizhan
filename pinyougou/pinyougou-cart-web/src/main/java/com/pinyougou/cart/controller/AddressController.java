package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.user.service.AddressService;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/address")
@RestController
public class AddressController {

    @Reference
    private AddressService addressService;

    /**
     * 新增
     * @param address 实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbAddress address){
        try {
            addressService.add(address);

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
    public TbAddress findOne(@PathVariable Long id){
        return addressService.findOne(id);
    }

    /**
     * 修改
     * @param address 实体
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbAddress address){
        try {
            addressService.update(address);
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
            addressService.deleteByIds(ids);
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
     * @param address 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbAddress> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbAddress address) {
        return addressService.search(pageNum, pageSize, address);
    }

    /**
     * 获取当前登录用户的地址列表
     * @return 地址列表
     */
    @GetMapping("/findAddressList")
    public List<TbAddress> findAddressList(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbAddress param = new TbAddress();
        param.setUserId(userId);
        return addressService.findByWhere(param);
    }

}
