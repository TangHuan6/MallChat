package com.th.mallchat.common.user.service.cache;

import com.th.mallchat.common.user.dao.BlackDao;
import com.th.mallchat.common.user.dao.UserRoleDao;
import com.th.mallchat.common.user.domain.entity.Black;
import com.th.mallchat.common.user.domain.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserCache {

    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private BlackDao blackDao;

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
}
