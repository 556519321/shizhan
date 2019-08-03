package com.pinyougou.service;

import com.github.pagehelper.PageInfo;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T> {
    //根据主键查询
    //Object也是可以的
    T findOne(Serializable id);

    //查询全部
    List<T> findAll();

    //条件查询
    List<T> findByWhere(T t);

    /**
     * 分页查询
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 分页信息对象（总记录数、总页数、页号、页大小、列表）
     */
    PageInfo<T> findPage(Integer pageNum, Integer pageSize);

    /**
     * 条件分页查询
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param t 条件对象
     * @return 分页信息对象（总记录数、总页数、页号、页大小、列表）
     */
    PageInfo<T> findPage(Integer pageNum, Integer pageSize, T t);

    //选择性新增
    void add(T t);

    //选择性更新
    void update(T t);

    /**
     * 批量删除
     * @param ids 批量删除
     */
    void deleteByIds(Serializable [] ids);

}
