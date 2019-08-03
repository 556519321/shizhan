package com.pinyougou.user.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface UserService extends BaseService<TbUser> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param user 搜索条件
     * @return 分页信息
     */
    PageInfo<TbUser> search(Integer pageNum, Integer pageSize, TbUser user);

    /**
     * 发送验证码
     * @param phone 手机号
     */
    void sendSmsCode(String phone);

    /**
     * 将用户输入的手机号与redis中的对比
     * @param phone 手机号
     * @param smsCode 验证码
     * @return ture or false
     */
    boolean checkSmsCode(String phone, String smsCode);
}
