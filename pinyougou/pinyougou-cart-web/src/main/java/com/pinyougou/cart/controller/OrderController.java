package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Reference
    private OrderService orderService;

    /**
     * 新增
     * @param order 实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbOrder order){
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            order.setUserId(userId);
            //订单来源；PC
            order.setSourceType("2");
            String outTradeOut = orderService.addOrder(order);

            return Result.ok(outTradeOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("提交订单失败");
    }

    /**
     * 根据主键查询
     * @param id 主键
     * @return 实体
     */
    @GetMapping("/findOne/{id}")
    public TbOrder findOne(@PathVariable Long id){
        return orderService.findOne(id);
    }

    /**
     * 修改
     * @param order 实体
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbOrder order){
        try {
            orderService.update(order);
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
            orderService.deleteByIds(ids);
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
     * @param order 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbOrder> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbOrder order) {
        return orderService.search(pageNum, pageSize, order);
    }

}
