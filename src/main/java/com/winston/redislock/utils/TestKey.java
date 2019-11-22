package com.winston.redislock.utils;

/**
 * @ClassName TestKey
 * @Author: Winston
 * @Description: TODO
 * @Date:Create：in 2019/11/22 16:01
 * @Version：
 */
public class TestKey extends BasePrefix {

    public TestKey(int expireSeconds, String preFix) {
        super(expireSeconds, preFix);
    }

    public TestKey(String preFix) {
        super(preFix);
    }

    public static TestKey COUNT_KEY = new TestKey("test");
    public static TestKey COUNT_LOCK = new TestKey(10, "lock");
}
