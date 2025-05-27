package com.th.mallchat.common.common.config;

import com.th.mallchat.common.common.factory.MyThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer {
    /**
     * 项目共用线程池
     */
    public static final String MALLCHAT_EXECUTOR = "mallchatExecutor";
    /**
     * websocket通信线程池
     */
    public static final String WS_EXECUTOR = "websocketExecutor";

    /**
     * 使用@Asysnc 但没有指定线程池名时 spring会默认调用这个方法来获取线程池
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        return mallchatExecutor();
    }

    /**
     *  这里使用的是spring的ThreadPoolTaskExecutor线程池 而不是JDK的ThreadPoolExecutor
     *  ThreadPoolTaskExecutor是spring对ThreadPoolExecutor的封装 支持spring的生命周期管理 支持@Async注解
     *  ThreadPoolExecutor需要手动管理线程的生命周期shutdown
     *  Spring 异步任务的执行器默认是 TaskExecutor，并且必须是 Spring 容器管理的 Bean
     *  ThreadPoolExecutor 不是 Spring 管理的 Bean，不能被 @Async 自动识别和使用
     *  ThreadPoolExecutor实现了disposableBean接口 当spring容器关闭时会回调ExecutorConfigurationSupport.destroy()方法
     *  waitForTasksToCompleteOnShutdown默认是false 是否等待任务完成后再关闭线程池
     */
    //创建 mallchatExecutor 线程池
    @Bean(MALLCHAT_EXECUTOR)
    //表示这是默认线程池，当有多个线程池时优先选这个
    @Primary
    public ThreadPoolTaskExecutor mallchatExecutor() {
//        @Override
//        public void destroy() {
//            shutdown();
//        }
//        public void shutdown() {
//            if (logger.isDebugEnabled()) {
//                logger.debug("Shutting down ExecutorService" + (this.beanName != null ? " '" + this.beanName + "'" : ""));
//            }
//            if (this.executor != null) {
//                if (this.waitForTasksToCompleteOnShutdown) {
//                    this.executor.shutdown();
//                }
//                else {
//                    for (Runnable remainingTask : this.executor.shutdownNow()) {
                          //这个方法默认是空实现（你可以扩展它，添加任务清理逻辑） 作用是对 shutdownNow() 失败的任务做一些“善后处理”
//                        cancelRemainingTask(remainingTask);
//                    }
//                }
//                awaitTerminationIfNecessary(this.executor);
//            }
//        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(true);//优雅停机
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("mallchat-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//满了调用线程执行，认为重要任务
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }

    @Bean(WS_EXECUTOR)
    public ThreadPoolTaskExecutor websocketExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);//支持同时推送1000人
        executor.setThreadNamePrefix("websocket-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());//满了直接丢弃，默认为不重要消息推送
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }

}