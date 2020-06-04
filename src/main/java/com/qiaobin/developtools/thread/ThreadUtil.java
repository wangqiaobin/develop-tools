package com.qiaobin.developtools.thread;

/**
 * @author qiaobinwang@qq.com
 * @version 1.0.0
 * @date 2020-05-25 10:33
 */
public class ThreadUtil {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
