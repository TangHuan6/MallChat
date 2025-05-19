package com.th.mallchat.common.common.factory;

import com.th.mallchat.common.common.thread.MyUncaughtExceptionHandler;
import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;

@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {
    /**
     *
     * ThreadPoolTaskExecutor线程池 他不会去捕获异常
     * 当出现未被捕获异常时 JVM会调用dispatchUncaughtException
     * 找到处理handler并调用uncaughtException方法
     * 我们就可以自定义handler然后给
     * 线程thread.setUncaughtExceptionHandler
     * 设置我们的自定义handler 记录日志
     * 但是用ThreadPoolTaskExecutor线程池
     * 每一个线程都是由线程工厂去创建的 默认的线程工厂并没有给每个线程去设置UncaughtExceptionHandler
     * 但是 ThreadPoolTaskExecutor有一个setThreadFactory方法
     * 我们可以定义线程工厂 但是线程工厂我们不能随便定义 所以就只有在原来的线程工厂基础上添加功能
     * 那如何去完成呢
     * 如果我们把这个线程工厂换了，那么它的线程创建方法就会失效。线程名，优先级啥的全都得我们一并做了。而我们只是想扩展一个线程捕获。
     * 这时候一个设计模式浮出脑海，装饰器模式
     * 装饰器模式不会改变原有的功能，而是在功能前后做一个扩展点 。完全适合我们这次的改动。
     *
     *
     */

    private static final MyUncaughtExceptionHandler MY_UNCAUGHT_EXCEPTION_HANDLER = new MyUncaughtExceptionHandler();
    private ThreadFactory original;
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = original.newThread(r);
        thread.setUncaughtExceptionHandler(MY_UNCAUGHT_EXCEPTION_HANDLER);
        return thread;
    }
}
