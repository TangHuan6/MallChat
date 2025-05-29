package com.th.mallchat.common.user.service;


import com.th.mallchat.common.common.domain.vo.requesr.PageBaseReq;
import com.th.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.th.mallchat.common.common.domain.vo.response.PageBaseResp;
import com.th.mallchat.common.user.domain.vo.request.CursorPageBaseReq;
import com.th.mallchat.common.user.domain.vo.request.FriendApplyReq;
import com.th.mallchat.common.user.domain.vo.request.FriendApproveReq;
import com.th.mallchat.common.user.domain.vo.request.FriendCheckReq;
import com.th.mallchat.common.user.domain.vo.response.FriendApplyResp;
import com.th.mallchat.common.user.domain.vo.response.FriendCheckResp;
import com.th.mallchat.common.user.domain.vo.response.FriendResp;
import com.th.mallchat.common.user.domain.vo.response.FriendUnreadResp;

/**
 * @author : limeng
 * @description : 好友
 */
public interface FriendService {


    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request);

    void applyApprove(Long uid, FriendApproveReq request);

    void apply(Long uid, FriendApplyReq request);

    FriendCheckResp check(Long uid, FriendCheckReq request);

    void deleteFriend(Long uid, Long targetUid);

    PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request);

    FriendUnreadResp unread(Long uid);
}
