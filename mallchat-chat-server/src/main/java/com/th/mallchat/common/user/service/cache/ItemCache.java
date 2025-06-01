package com.th.mallchat.common.user.service.cache;

import com.th.mallchat.common.user.dao.ItemConfigDao;
import com.th.mallchat.common.user.domain.entity.ItemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemCache {

    @Autowired
    private ItemConfigDao itemConfigDao;


    /**
     *
     * @param itemType
     * @return
     * value 表示缓存的名称（类似于命名空间）
     * key 是缓存的 key，如果不指定，会使用所有参数组合
     * 当调用这个方法时，如果缓存中有结果，则直接返回；否则执行方法并缓存结果
     * 说明：
     * spring在启动时通过@EnableCaching开启了基于AOP的缓存拦截器 CacheInterceptor extends AbstractCacheInterceptor
     * 当调用一个被@Cacheable标注的方法时 AOP会拦截它
     * if (cache.containsKey(key)) {
     *     return cache.get(key); // 命中缓存
     * } else {
     *     Object result = method.invoke(); // 执行目标方法
     *     cache.put(key, result);         // 放入缓存
     *     return result;
     * }
     * SpringBoot默认使用ConcurrentHashMap实现本地内存缓存 也可以自己配置Redis Caffeine等三方缓存
     * @Cacheable 当调用方法时，先从缓存中查找是否有值，有就直接返回，不执行方法；没有才执行方法，并把结果放进缓存中。
     * @CachePut 每次调用方法都执行方法逻辑，并将结果更新到缓存中。适合用于新增或更新数据时同时更新缓存。
     * @CacheEvict 从缓存中移除指定的 key 或清空整个缓存，常用于删除数据或更新后手动清理旧数据。
     */
    @Cacheable(cacheNames = "item",key = "'itemsByType'+#itemType")
    public List<ItemConfig> getByType(Integer itemType) {
        return itemConfigDao.getByType(itemType);
    }

    /**
     * 单引号 'itemsByType' 是字符串字面量，即常量字符串 "itemsByType"。
     *
     * #itemType 是Spring EL 表达式，代表方法参数 itemType 的值。
     *
     * 换句话说，这个 key 表达式的结果是：
     * "itemsByType" + itemType的值
     * @param itemType
     */
    @CacheEvict(cacheNames = "item",key = "'itemsByType'+#itemType")
    public void evictByType(Integer itemType) {
    }

    @Cacheable(cacheNames = "item",key = "'item:'+#itemId")
    public ItemConfig getById(Long itemId) {
        return itemConfigDao.getById(itemId);
    }
}
