package com.th.mallchat.common.user.service.impl;

import com.th.mallchat.common.common.exception.BusinessException;
import com.th.mallchat.common.common.utils.AssertUtil;
import com.th.mallchat.common.user.dao.UserBackpackDao;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.entity.ItemConfig;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.entity.UserBackpack;
import com.th.mallchat.common.user.domain.enums.ItemEnum;
import com.th.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.th.mallchat.common.user.domain.vo.request.ModifyNameReq;
import com.th.mallchat.common.user.domain.vo.response.BadgeResp;
import com.th.mallchat.common.user.domain.vo.response.UserInfoResp;
import com.th.mallchat.common.user.service.UserService;
import com.th.mallchat.common.user.service.adapter.UserAdapter;
import com.th.mallchat.common.user.service.cache.ItemCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    private ItemCache itemCache;

    @Override
    @Transactional
    public Long register(User user) {
        userDao.save(user);
        //todo 用户注册的事件
        return user.getId();
    }

    @Override
    public UserInfoResp getUserInfo(Long uid) {
        User user = userDao.getById(uid);
        Integer modifyNameCount = userBackpackDao.getCountByVaildId(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        return UserAdapter.buildUserInfo(user, modifyNameCount);
    }

    @Override
    @Transactional
    public void modifyName(Long uid, String name) {
        User oldUser = userDao.getByName(name);
        AssertUtil.isEmpty(oldUser,"名称重复，请换一个哦");
        UserBackpack firstValidItem = userBackpackDao.getFirstValidItem(uid,ItemEnum.MODIFY_NAME_CARD.getId());
        AssertUtil.isNotEmpty(firstValidItem,"改名卡次数已耗尽");
        boolean useSucess = userBackpackDao.useItem(firstValidItem.getId());
        if (useSucess){
            userDao.modifyName(uid,name);
        }
    }

    @Override
    public List<BadgeResp> badges(Long uid) {
        //查询所有徽章
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        //查询用户拥有的徽章
        List<UserBackpack> backpacks = userBackpackDao.getByItemIds(uid, itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList()));
        //查询用户当前佩戴的标签
        User user = userDao.getById(uid);
        return UserAdapter.buildBadgeResp(itemConfigs, backpacks, user);
    }
}
