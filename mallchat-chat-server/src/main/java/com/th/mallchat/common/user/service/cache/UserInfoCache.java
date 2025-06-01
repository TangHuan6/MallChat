package com.th.mallchat.common.user.service.cache;

import com.th.mallchat.common.common.constant.RedisKey;
import com.th.mallchat.common.common.service.cache.AbstractRedisStringCache;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserInfoCache extends AbstractRedisStringCache<Long, User> {

    @Autowired
    private UserDao userDao;

    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_INFO_STRING,uid);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, User> load(List<Long> uidList) {
        List<User> needLoadUserList = userDao.listByIds(uidList);
        return needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
