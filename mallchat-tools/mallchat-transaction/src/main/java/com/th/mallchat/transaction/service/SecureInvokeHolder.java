package com.th.mallchat.transaction.service;

import java.util.Objects;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-10-02
 *
 * 这是一个线程级别的标记工具类,用来标记当前线程是否正在执行通过SecureInvoke注册的重试任务 防止方法递归重复拦截
 * 在系统中使用了注解方法@SecureInvoke 第一次执行失败后，记录到了数据库中。然后，系统调度器开始异步重试它。
 * 假设不加 SecureInvokeHolder，你会发生什么？
 * ❌ 会无限递归！
 * 重试任务再次调用了 placeOrder(...)
 * 因为它仍然带着 @SecureInvoke 注解，切面又会拦截它！
 * 切面又会去保存一份记录 + 注册重试
 * 然后再次重试，再次记录...
 */
public class SecureInvokeHolder {
    private static final ThreadLocal<Boolean> INVOKE_THREAD_LOCAL = new ThreadLocal<>();

    public static boolean isInvoking() {
        return Objects.nonNull(INVOKE_THREAD_LOCAL.get());
    }

    public static void setInvoking() {
        INVOKE_THREAD_LOCAL.set(Boolean.TRUE);
    }

    public static void invoked() {
        INVOKE_THREAD_LOCAL.remove();
    }
}
