package com.th.mallchat.common.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.th.mallchat.common.common.annotation.RedissonLock;
import com.th.mallchat.common.common.service.LockService;
import com.th.mallchat.common.common.utils.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Description: 分布式锁切面
 */

/**
 * 如果先开启事务再加锁的话 假如A和B都进入事务 A拿到了锁 然后进行扣减库存操作 并解锁 此时A的事务还没提交
 * B就拿到了锁 B就出现了重复读 所以B 也继续扣减库存提交事务
 * 这就出现了并发环境下的超卖问题。所以我们必须先加锁再开启事务 每次只有一个线程能读写数据，其他排队 从而解决了这个问题
 *
 * 补充复习：
 * 脏读：一个事务读取了另一个未提交事务所做的修改。
 * 幻读：一个事务在执行两次相同条件的查询时，第二次查询返回了“第一次没有看到的新数据行”。
 * 开启一个事务后，这段时间内我的“视图”应该是一致的，不应该在中途发生变化。
 */
@Slf4j
@Aspect
@Component
@Order(0)//确保比事务注解先执行，分布式锁在事务外
public class RedissonLockAspect {
    @Autowired
    private LockService lockService;

    /**
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     * SpringAop本质就是动态代理机制 被代理的类会在spring启动时替换成代理对象这个代理对象在调用方法前/后执行增强逻辑
     * @Pointcut 定义切入表达式 @Pointcut("execution(* com.example.demo.controller..*(..))")
     * @Before 方法前执行 @After方法后执行（无论是否异常） @AfterReturning正常返回时执行 @AfterThrowing 异常抛出后执行
     * @Around 环绕执行，可控制是否调用目标方法
     * joinPoint.proceed()：继续执行目标方法
     *
     */

    @Around("@annotation(com.th.mallchat.common.common.annotation.RedissonLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        String prefix = StrUtil.isBlank(redissonLock.prefixKey()) ? SpElUtils.getMethodKey(method) : redissonLock.prefixKey();//默认方法限定名+注解排名（可能多个）
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), redissonLock.key());
        return lockService.executeWithLockThrows(prefix + ":" + key, redissonLock.waitTime(), redissonLock.unit(), joinPoint::proceed);
    }

}
