package com.th.mallchat.common.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.th.mallchat.common.common.event.UserBlackEvent;
import com.th.mallchat.common.common.event.UserRegisterEvent;
import com.th.mallchat.common.common.exception.BusinessException;
import com.th.mallchat.common.common.utils.AssertUtil;
import com.th.mallchat.common.user.dao.BlackDao;
import com.th.mallchat.common.user.dao.ItemConfigDao;
import com.th.mallchat.common.user.dao.UserBackpackDao;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.dto.ItemInfoDTO;
import com.th.mallchat.common.user.domain.dto.SummeryInfoDTO;
import com.th.mallchat.common.user.domain.entity.Black;
import com.th.mallchat.common.user.domain.entity.ItemConfig;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.entity.UserBackpack;
import com.th.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.th.mallchat.common.user.domain.enums.ItemEnum;
import com.th.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.th.mallchat.common.user.domain.vo.request.*;
import com.th.mallchat.common.user.domain.vo.response.BadgeResp;
import com.th.mallchat.common.user.domain.vo.response.UserInfoResp;
import com.th.mallchat.common.user.service.UserService;
import com.th.mallchat.common.user.service.adapter.UserAdapter;
import com.th.mallchat.common.user.service.cache.ItemCache;
import com.th.mallchat.common.user.service.cache.UserCache;
import com.th.mallchat.common.user.service.cache.UserSummaryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    private ItemConfigDao itemConfigDao;

    @Autowired
    private ItemCache itemCache;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private BlackDao blackDao;

    @Autowired
    private UserCache userCache;

    @Autowired
    private UserSummaryCache userSummaryCache;

    @Override
    public Long register(User user) {
        userDao.save(user);
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this,user));
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

    @Override
    public void wearingBadge(Long uid, WearingBadgeReq req) {
        //确保有这个徽章
        Long badgeId = req.getBadgeId();
        UserBackpack firstValidItem = userBackpackDao.getFirstValidItem(uid, badgeId);
        AssertUtil.isNotEmpty(firstValidItem,"您没有这个徽章哦，快去达成条件获取吧");
        //确保物品类型是徽章
        ItemConfig itemConfig = itemConfigDao.getById(firstValidItem.getItemId());
        AssertUtil.equal(itemConfig.getType(),ItemTypeEnum.BADGE.getType(),"该物品不可佩戴");
        //佩戴徽章
        userDao.wearingBadge(uid, req.getBadgeId());
        //删除用户缓存
    }

    @Override
    @Transactional
    public void black(BlackReq req) {
        Long uid = req.getUid();
        Black user = new Black();
        user.setTarget(uid.toString());
        user.setType(BlackTypeEnum.UID.getType());
        blackDao.save(user);
        User byId = userDao.getById(uid);
        blackIp(byId.getIpInfo().getCreateIp());
        blackIp(byId.getIpInfo().getUpdateIp());
        applicationEventPublisher.publishEvent(new UserBlackEvent(this,byId));
    }

    @Override
    public List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req) {
        //需要前端同步的uid
        List<Long> uidList = getNeedSyncUidList(req.getReqList());
        //加载用户信息
        Map<Long, SummeryInfoDTO> batch = userSummaryCache.getBatch(uidList);
        return req.getReqList()
                .stream()
                .map(a -> batch.containsKey(a.getUid()) ? batch.get(a.getUid()) : SummeryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemInfoDTO> getItemInfo(ItemInfoReq req) {
        return req.getReqList().stream().map(a -> {
            ItemConfig itemConfig = itemCache.getById(a.getItemId());
            if (Objects.nonNull(a.getLastModifyTime()) && a.getLastModifyTime() >= itemConfig.getUpdateTime().getTime()){
                return ItemInfoDTO.skip(a.getItemId());
            }
            ItemInfoDTO dto = new ItemInfoDTO();
            dto.setItemId(itemConfig.getId());
            dto.setImg(itemConfig.getImg());
            dto.setDescribe(itemConfig.getDescribe());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<Long> getNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList) {
        List<Long> needSyncUidList = new ArrayList<>();
        List<Long> userModifyTime = userCache.getUserModifyTime(reqList.stream()
                .map(SummeryInfoReq.infoReq::getUid).collect(Collectors.toList()));
        for (int i = 0; i < reqList.size(); i++) {
            SummeryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = userModifyTime.get(i);
            if (Objects.isNull(infoReq.getLastModifyTime()) || (Objects.nonNull(modifyTime) && modifyTime > infoReq.getLastModifyTime())) {
                needSyncUidList.add(infoReq.getUid());
            }
        }
        return needSyncUidList;
    }

    private void blackIp(String ip) {
        if (StrUtil.isBlank(ip)){
            return;
        }
        try {
            Black user = new Black();
            user.setTarget(ip);
            user.setType(BlackTypeEnum.IP.getType());
            blackDao.save(user);
        }catch (Exception e){
            log.error("duplicate black ip:{}", ip);
        }
    }
}
