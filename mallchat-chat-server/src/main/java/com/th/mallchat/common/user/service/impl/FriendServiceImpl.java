package com.th.mallchat.common.user.service.impl;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.th.mallchat.common.common.annotation.RedissonLock;
import com.th.mallchat.common.common.domain.vo.requesr.PageBaseReq;
import com.th.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.th.mallchat.common.common.domain.vo.response.PageBaseResp;
import com.th.mallchat.common.common.event.listener.UserApplyEvent;
import com.th.mallchat.common.common.utils.AssertUtil;
import com.th.mallchat.common.user.dao.UserApplyDao;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.dao.UserFriendDao;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.entity.UserApply;
import com.th.mallchat.common.user.domain.entity.UserFriend;
import com.th.mallchat.common.user.domain.enums.ApplyStatusEnum;
import com.th.mallchat.common.user.domain.vo.request.CursorPageBaseReq;
import com.th.mallchat.common.user.domain.vo.request.FriendApplyReq;
import com.th.mallchat.common.user.domain.vo.request.FriendApproveReq;
import com.th.mallchat.common.user.domain.vo.request.FriendCheckReq;
import com.th.mallchat.common.user.domain.vo.response.FriendApplyResp;
import com.th.mallchat.common.user.domain.vo.response.FriendCheckResp;
import com.th.mallchat.common.user.domain.vo.response.FriendResp;
import com.th.mallchat.common.user.domain.vo.response.FriendUnreadResp;
import com.th.mallchat.common.user.service.FriendService;
import com.th.mallchat.common.user.service.adapter.FriendAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author : limeng
 * @description : 好友
 */
@Slf4j
@Service
public class FriendServiceImpl implements FriendService {


    @Autowired
    private UserDao userDao;

    @Autowired
    private UserFriendDao userFriendDao;

    @Autowired
    private UserApplyDao userApplyDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request) {
        CursorPageBaseResp<UserFriend> friendPage = userFriendDao.getFriendPage(uid, request);
        if (CollectionUtil.isEmpty(friendPage.getList())){
            return CursorPageBaseResp.empty();
        }
        List<Long> friendList = friendPage.getList().stream()
                .map(UserFriend::getFriendUid)
                .collect(Collectors.toList());
        List<User> userList = userDao.getFriendList(friendList);
        return CursorPageBaseResp.init(friendPage, FriendAdapter.buildFriend(friendPage.getList(), userList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void applyApprove(Long uid, FriendApproveReq request) {
        UserApply userApply = userApplyDao.getById(request.getApplyId());
        AssertUtil.isNotEmpty(userApply,"不存在申请记录");
        AssertUtil.equal(userApply.getTargetId(),uid,"不存在申请记录");
        AssertUtil.equal(userApply.getStatus(), ApplyStatusEnum.WAIT_APPROVAL.getCode(),"已同意好友申请");
        //同意
        userApplyDao.agree(request.getApplyId());
        //创建双方好友关系
        createFriend(uid, userApply.getUid());
        //todo 创建一个聊天房间

        //todo 发送一条同意消息

    }

    @Override
    @RedissonLock(key = "#uid")
    public void apply(Long uid, FriendApplyReq request) {
        UserFriend friend = userFriendDao.getByFriend(uid, request.getTargetUid());
        AssertUtil.isEmpty(friend,"你们已经是好友了");
        //是否有待审批的申请记录(自己的)
        UserApply selfApproving = userApplyDao.getFriendApproving(uid, request.getTargetUid());
        if (Objects.nonNull(selfApproving)){
            log.info("已有好友申请记录,uid:{}, targetId:{}", uid, request.getTargetUid());
            return;
        }
        //是否有待审批的申请记录(别人请求自己的)
        UserApply friendApproving = userApplyDao.getFriendApproving(request.getTargetUid(), uid);
        if (Objects.nonNull(friendApproving)) {
            ((FriendService) AopContext.currentProxy()).applyApprove(uid, new FriendApproveReq(friendApproving.getId()));
            return;
        }
        //申请入库
        UserApply insert = FriendAdapter.buildFriendApply(uid, request);
        userApplyDao.save(insert);
        //todo 申请事件 监听发送通知
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, insert));
    }

    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq request) {
        List<UserFriend> friendList = userFriendDao.getByFriends(uid, request.getUidList());
        Set<Long> friendUidSet = friendList.stream().map(UserFriend::getFriendUid).collect(Collectors.toSet());
        request.getUidList().stream().map(friendUid -> {
            FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
            friendCheck.setUid(friendUid);
            friendCheck.setIsFriend(friendUidSet.contains(friendUid));
            return friendCheck;
        }).collect(Collectors.toList());
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long uid, Long friendUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, friendUid);
        if (CollectionUtil.isEmpty(userFriends)){
            log.info("没有好友关系：{},{}", uid, friendUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        userFriendDao.removeByIds(friendRecordIds);
        //todo 禁用房间

    }

    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request) {
        IPage<UserApply> userApplyIPage = userApplyDao.friendApplyPage(uid, request.plusPage());
        if (CollectionUtil.isEmpty(userApplyIPage.getRecords())){
            return PageBaseResp.empty();
        }
        //将这些申请列表设为已读
        readApples(uid, userApplyIPage);
        //返回消息
        return PageBaseResp.init(userApplyIPage, FriendAdapter.buildFriendApplyList(userApplyIPage.getRecords()));
    }

    @Override
    public FriendUnreadResp unread(Long uid) {
        Integer unReadCount = userApplyDao.getUnReadCount(uid);
        return new FriendUnreadResp(unReadCount);
    }

    private void readApples(Long uid, IPage<UserApply> userApplyIPage) {
        List<Long> applyIds = userApplyIPage.getRecords()
                .stream().map(UserApply::getId)
                .collect(Collectors.toList());
        userApplyDao.readApples(uid,applyIds);
    }

    private void createFriend(Long uid, Long targetUid) {
        UserFriend userFriend1 = new UserFriend();
        userFriend1.setUid(uid);
        userFriend1.setFriendUid(targetUid);
        UserFriend userFriend2 = new UserFriend();
        userFriend1.setUid(targetUid);
        userFriend1.setFriendUid(uid);
        userFriendDao.saveBatch(Lists.newArrayList(userFriend1, userFriend2));
    }
}
