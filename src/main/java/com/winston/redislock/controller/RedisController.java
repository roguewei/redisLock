package com.winston.redislock.controller;

import com.winston.redislock.utils.RedisUtil;
import com.winston.redislock.utils.TestKey;
import io.lettuce.core.RedisClient;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisController
 * @Author: Winston
 * @Description: TODO
 * @Date:Create：in 2019/11/22 15:59
 * @Version：
 */
@RestController
@RequestMapping("/test")
public class RedisController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * @auther: Winston
     * @Description: 该方法减库存是不安全的
     * @param:
     * @return:
     * @date: 2019/11/22 16:55
     */
//    @GetMapping("/test")
//    public String test(){
//        // 该方法减库存是不安全的
//        int count = redisUtil.get(TestKey.COUNT_KEY, "count", Integer.class);
//        if(count > 0){
//            count = count-1;
//            redisUtil.set(TestKey.COUNT_KEY, "count", count);
//            System.out.println("剩余库存： " + count);
//        }else{
//            System.out.println("减库失败");
//        }
//        return "success";
//    }

    /**
     * @auther: Winston
     * @Description: 该方法在单个服务时减库存是安全的，只适合单个服务器模式
     * 当有负载均衡时这种加锁是不安全的
     * 因为这是跨进程的状态，this代表的就不是当前的对象了
     * @param:
     * @return:
     * @date: 2019/11/22 16:54
     */
//    @GetMapping("/test")
//    public String test(){
//        synchronized (this){
//            int count = redisUtil.get(TestKey.COUNT_KEY, "count", Integer.class);
//            if(count > 0){
//                count = count-1;
//                redisUtil.set(TestKey.COUNT_KEY, "count", count);
//                System.out.println("剩余库存： " + count);
//            }else{
//                System.out.println("减库失败");
//            }
//        }
//        return "success";
//    }

//    /**
//     * @auther: Winston
//     * @Description: 使用分布式锁
//     * 但是如果其中一台机器宕机，没有释放锁，
//     * 则其他机器上的服务就不能获取锁，从而形成死锁问题
//     * 所以为了防止这种情况，需要给锁设置超时时间
//     * @param:
//     * @return:
//     * @date: 2019/11/22 16:54
//     */
//    @GetMapping("/test")
//    public String test(){
//
//        // 避免其他线程进来删掉其他线程的锁，所以需要值唯一
//        String uuid = UUID.randomUUID().toString();
//        // 因为多个服务请求的都是同一个redis，所以可以在redis中存数据当做锁
//        // 加锁
//        boolean lock = redisUtil.setnx(TestKey.COUNT_LOCK, "lock", uuid);
//
//        System.out.println(lock);
//        // 是否存在锁
//        if(!lock){
//            return "fail";
//        }
//
//        // 判断当前线程是否还持有锁，如果持有，再设置超时时间，避免业务逻辑没有执行完而锁的过期时间到了
//        // 利用多线程动态更新锁超时时间
//
//        try {
//            int count = redisUtil.get(TestKey.COUNT_KEY, "count", Integer.class);
//            if(count > 0){
//                count = count-1;
//                redisUtil.set(TestKey.COUNT_KEY, "count", count);
//                System.out.println("剩余库存： " + count);
//            }else{
//                System.out.println("减库失败");
//            }
//        }finally {
//            // 避免执行逻辑时抛出异常而没有释放锁
//            // 释放锁
//            // 判断是否是当前线程生成的值，能对应的话才执行释放
//            if(uuid.equals(redisUtil.get(TestKey.COUNT_LOCK, "lock", String.class)))
//                redisUtil.del(TestKey.COUNT_LOCK, "lock");
//        }
//
//        return "success";
//    }

    /**
     * @auther: Winston
     * @Description: 使用分布式锁——Redisson
     *
     * @param:
     * @return:
     * @date: 2019/11/22 16:54
     */
    @GetMapping("/test")
    public String test(){

        String lockKey = "TestKey:lock";
        // 拿到锁
        RLock lock = redissonClient.getLock(lockKey);

        // 加锁并设置锁超时时间
        lock.lock(30, TimeUnit.SECONDS);

        try {
            int count = redisUtil.get(TestKey.COUNT_KEY, "count", Integer.class);
            if(count > 0){
                count = count-1;
                redisUtil.set(TestKey.COUNT_KEY, "count", count);
                System.out.println("剩余库存： " + count);
            }else{
                System.out.println("减库失败");
            }
        }finally {
            // 释放锁
            lock.unlock();
        }

        return "success";
    }

    @GetMapping("/get")
    public String get(String key){
        String count = redisUtil.get(TestKey.COUNT_KEY, key, String.class);
        System.out.println("剩余库存： " + count);
        return "success";
    }

    @GetMapping("/setnx")
    public String setnx(String key, String value){
        boolean result = redisUtil.setnx(TestKey.COUNT_LOCK, key, value);
        System.out.println(result);
        return "success";
    }

    @GetMapping("/set")
    public String set(String key, String value){
        redisUtil.set(TestKey.COUNT_KEY, key, value);
        return "success";
    }

}
