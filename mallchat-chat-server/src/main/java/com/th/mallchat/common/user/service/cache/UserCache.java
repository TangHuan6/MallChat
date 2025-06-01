package com.th.mallchat.common.user.service.cache;

import cn.hutool.core.collection.CollectionUtil;
import com.th.mallchat.common.common.constant.RedisKey;
import com.th.mallchat.common.common.utils.RedisUtils;
import com.th.mallchat.common.user.dao.BlackDao;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.dao.UserRoleDao;
import com.th.mallchat.common.user.domain.entity.Black;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserCache {

    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private BlackDao blackDao;

    @Autowired
    private UserDao userDao;

    @Cacheable(cacheNames = "user",key = "'roles'+#uid")
    public Set<Long> getRoleSet(Long uid) {
        List<UserRole> userRoles = userRoleDao.listByUid(uid);
        return userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());
    }

    @Cacheable(cacheNames = "user",key = "'blackList'")
    public Map<Integer, Set<String>> getBlackMap() {
        List<Black> list = blackDao.list();
        Map<Integer, List<Black>> collect = blackDao.list().stream().collect(Collectors.groupingBy(Black::getType));
        Map<Integer, Set<String>> result = new HashMap<>(collect.size());
        for (Map.Entry<Integer, List<Black>> entry : collect.entrySet()) {
            result.put(entry.getKey(),entry.getValue().stream().map(Black::getTarget).collect(Collectors.toSet()));
        }
        return result;
    }

    public List<Long> getUserModifyTime(List<Long> uidList) {
        List<String> keys = uidList.stream().map(uid -> RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid)).collect(Collectors.toList());
        return RedisUtils.mget(keys,Long.class);
    }

    /**
     * 获取用户信息，盘路缓存模式
     */
    public User getUserInfo(Long uid) {//todo 后期做二级缓存
        return getUserInfoBatch(Collections.singleton(uid)).get(uid);
    }

    /**
     * 获取用户信息，盘路缓存模式
     */
    public Map<Long, User> getUserInfoBatch(Set<Long> uids) {
        //批量组装key
        List<String> keys = uids.stream().map(uid -> RedisKey.getKey(RedisKey.USER_INFO_STRING, uid)).collect(Collectors.toList());
        //批量get
        List<User> mget = RedisUtils.mget(keys, User.class);
        Map<Long, User> userMap = mget.stream().filter(Objects::nonNull).collect(Collectors.toMap(user -> user.getId(), user -> user));
        List<Long> needLoadUidList = uids.stream().filter(uid -> !userMap.containsKey(uid)).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(needLoadUidList)){
            //批量load
            List<User> needLoadUserList = userDao.listByIds(needLoadUidList);
            Map<String, User> redisMap = needLoadUserList.stream().collect(Collectors.toMap(user -> RedisKey.getKey(RedisKey.USER_INFO_STRING, user.getId()), user -> user));
            RedisUtils.mset(redisMap,5*60);
            userMap.putAll(needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity())));
        }
        return userMap;
    }
}
