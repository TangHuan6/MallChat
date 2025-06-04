package com.th.mallchat.transaction.config;

import com.th.mallchat.transaction.annotation.SecureInvokeConfigurer;
import com.th.mallchat.transaction.aspect.SecureInvokeAspect;
import com.th.mallchat.transaction.dao.SecureInvokeRecordDao;
import com.th.mallchat.transaction.mapper.SecureInvokeRecordMapper;
import com.th.mallchat.transaction.service.MQProducer;
import com.th.mallchat.transaction.service.SecureInvokeService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-06
 */
@Configuration
@EnableScheduling
@MapperScan(basePackageClasses = SecureInvokeRecordMapper.class)
//将 SecureInvokeAspect（切面）和 SecureInvokeRecordDao（DAO 实现）注入到容器中。
@Import({SecureInvokeAspect.class, SecureInvokeRecordDao.class})
public class TransactionAutoConfiguration {

    @Nullable
    protected Executor executor;

    /**
     * Collect any {@link AsyncConfigurer} beans through autowiring.
     */

    /**
     * ObjectProvider<SecureInvokeConfigurer> configurers
     * 这是一个 延迟注入器，不会强制立刻注入 bean，也不会因找不到而报错，常用于“可选依赖”。
     *
     * SingletonSupplier.of(...) 是 Spring 提供的一个懒加载封装器，它确保 lambda 表达式只执行一次，适合成本较高的计算。
     * configurers.stream().collect(...) 把所有实现了 SecureInvokeConfigurer 接口的 bean 都收集起来。
     * @param configurers
     */
    @Autowired
    void setConfigurers(ObjectProvider<SecureInvokeConfigurer> configurers) {
        Supplier<SecureInvokeConfigurer> configurer = SingletonSupplier.of(() -> {
            List<SecureInvokeConfigurer> candidates = configurers.stream().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                return null;
            }
            if (candidates.size() > 1) {
                throw new IllegalStateException("Only one SecureInvokeConfigurer may exist");
            }
            return candidates.get(0);
        });
        executor = Optional.ofNullable(configurer.get()).map(SecureInvokeConfigurer::getSecureInvokeExecutor).orElse(ForkJoinPool.commonPool());
    }

    @Bean
    public SecureInvokeService getSecureInvokeService(SecureInvokeRecordDao dao) {
        return new SecureInvokeService(dao, executor);
    }

    @Bean
    public MQProducer getMQProducer() {
        return new MQProducer();
    }
}
