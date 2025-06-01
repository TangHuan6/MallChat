package com.th.mallchat.common.common.service.cache;

import cn.hutool.core.collection.CollectionUtil;
import com.th.mallchat.common.common.utils.RedisUtils;
import io.swagger.models.auth.In;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板方法模式是一种行为型设计模式，它在父类中定义一个算法的骨架（模板），并允许子类在不改变结构的前提下重写某些步骤的实现。
 * 模板方法模式的核心是“将公共流程固定在抽象类中，而差异化步骤交由子类实现”。
 *
 * 抽象类特点：
 * 抽象类不能被实例化（不能 new 出对象）。
 * 可以包含构造方法、成员变量、静态方法。
 * 可以包含抽象方法（即没有方法体的方法），也可以包含非抽象方法（即已实现的方法）。
 * 抽象方法只能定义在抽象类中。
 * 抽象类可以被继承，子类必须实现其中的抽象方法，除非子类也是抽象类。
 *
 */
public abstract class AbstractRedisStringCache<IN,OUT> implements BatchCache<IN,OUT>{

    private Class<OUT> outClass;


    protected abstract String getKey(IN req);

    protected abstract Long getExpireSeconds();

    protected abstract Map<IN,OUT> load(List<IN> req);

    @Override
    public OUT get(IN req) {
        return getBatch(Collections.singletonList(req)).get(req);
    }

    @Override
    public Map<IN, OUT> getBatch(List<IN> req) {
        if (CollectionUtil.isEmpty(req)) {//防御性编程
            return new HashMap<>();
        }
        //去重
        req = req.stream().distinct().collect(Collectors.toList());
        //组装key
        List<String> keys = req.stream().map(this::getKey).collect(Collectors.toList());
        List<OUT> valueList = RedisUtils.mget(keys, outClass);
        //差集计算
        List<IN> loadReqs = new ArrayList<>();
        for (int i = 0; i < valueList.size(); i++) {
            if (Objects.isNull(valueList.get(i))){
                loadReqs.add(req.get(i));
            }
        }
        Map<IN, OUT> load = new HashMap<>();
        //不足的重新加载进redis
        if (CollectionUtil.isNotEmpty(loadReqs)) {
            //批量load
            load = load(loadReqs);
            Map<String, OUT> loadMap = load.entrySet().stream()
                    .map(a -> Pair.of(getKey(a.getKey()), a.getValue()))
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
            RedisUtils.mset(loadMap,getExpireSeconds());
        }

        //组装最后的结果
        Map<IN, OUT> resultMap = new HashMap<>();
        for (int i = 0; i < req.size(); i++) {
            IN in = req.get(i);
            OUT out = Optional.ofNullable(valueList.get(i))
                    .orElse(load.get(in));
            resultMap.put(in, out);
        }
        return resultMap;
    }

    @Override
    public void delete(IN req) {
        deleteBatch(Collections.singletonList(req));
    }

    @Override
    public void deleteBatch(List<IN> req) {
        List<String> keys = req.stream().map(this::getKey).collect(Collectors.toList());
        RedisUtils.del(keys);
    }
}
