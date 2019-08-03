package com.pinyougou.shop.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;

    /**
     * 实现用户认证
     * @param username 用户在页面中输入的用户名
     * @return 用户对象
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //设置角色列表（应该也要根据用户名从数据库中查询）
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //根据用户名查询已经审核通过的商家
        TbSeller seller = sellerService.findOne(username);
        if(seller != null && "1".equals(seller.getStatus())){
            return new User(username, seller.getPassword(), authorities);
        }

        //将页面中接收到的用户名和密码与现在返回的用户信息
        //return new User(username, "123456", authorities);
        return null;
    }
}
