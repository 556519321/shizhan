package com.pinyougou.common.util;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * 主要用于在分布式环境下对于共享资源的操作进行锁定
 */
public class RedisLock {

    private RedisTemplate redisTemplate;

    private static final ThreadLocal<String> tlExpireTime = new ThreadLocal<>();

    //获取锁最长等待时间；10秒
    private static final long WAIT_TIME_OUT = 10 * 1000;

    //锁的过期时间；3秒
    private static final int LOCK_EXPIRE_TIME = 3 * 1000;

    public RedisLock(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在一定的限定时间内 获取某一类资源或操作的锁
     *
     * @param key 键名（可以随便指定；一般为某一类资源或操作命名）
     * @return true or false
     */
    public Boolean lock(String key) throws InterruptedException {
        long timeOut = WAIT_TIME_OUT;
        while (timeOut > 0) {
            long expireTime = System.currentTimeMillis() + LOCK_EXPIRE_TIME;
            Boolean isExists = redisTemplate.boundValueOps(key).setIfAbsent(expireTime);
            //获取锁成功，直接返回
            if (isExists) {
                //记录本线程设置的过期时间，释放锁的时候可以根据其进行判断是否该释放锁
                tlExpireTime.set(expireTime+"");
                return true;
            }

            //获取锁失败，查看是否锁已经超时
            long oldExpireTime = (long) redisTemplate.boundValueOps(key).get();
            //锁已经超时
            if (oldExpireTime < System.currentTimeMillis()) {
                //自行尝试解锁并获取锁
                long newExpireTime = System.currentTimeMillis() + LOCK_EXPIRE_TIME;
                long oldExpireTime2 = (long) redisTemplate.boundValueOps(key).getAndSet(newExpireTime);
                if (oldExpireTime == oldExpireTime2) {
                    //没有被其它线程设置并获取，说明获取锁成功
                    tlExpireTime.set(newExpireTime+"");
                    return true;
                }
            }

            //沉睡随机几百毫秒再去尝试获取锁
            long sleepTime = (long) (Math.random() * 1000);
            //递减等待获取锁的时间
            timeOut = timeOut - sleepTime;

            Thread.sleep(sleepTime);
        }
        return false;
    }

    /**
     * 释放某一类资源或操作的锁
     *
     * @param key key 键名（与加锁时候的key要一致）
     */
    public void unlock(String key) {
        long expireTimeMillis = (long) redisTemplate.boundValueOps(key).get();
        //获取当时设置到redis的过期时间
        String strExpireTime = tlExpireTime.get();
        if (strExpireTime != null && strExpireTime.length() > 0) {
            long oldExpireTime = Long.parseLong(strExpireTime);
            if ((System.currentTimeMillis() < oldExpireTime) || (oldExpireTime == expireTimeMillis)) {
                //如果释放锁的时候没有超时才需要释放锁；不然这是可能被其它线程已经获取到的锁，不应该把其它线程获取到的锁给释放了
                redisTemplate.delete(key);
            }
        }
        //只要调用释放锁，不管有没释放锁，但是线程变量都已经没有存在意义
        tlExpireTime.remove();
    }
}
