package com.th.mallchat.common.user.controller;

import com.th.mallchat.common.common.domain.vo.requesr.PageBaseReq;
import com.th.mallchat.common.common.domain.vo.response.ApiResult;
import com.th.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.th.mallchat.common.common.domain.vo.response.PageBaseResp;
import com.th.mallchat.common.common.utils.RequestHolder;
import com.th.mallchat.common.user.domain.vo.request.*;
import com.th.mallchat.common.user.domain.vo.response.FriendApplyResp;
import com.th.mallchat.common.user.domain.vo.response.FriendCheckResp;
import com.th.mallchat.common.user.domain.vo.response.FriendResp;
import com.th.mallchat.common.user.domain.vo.response.FriendUnreadResp;
import com.th.mallchat.common.user.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 好友相关接口
 * </p>
 *
 * @author th
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/capi/user/friend")
@Api(tags = "好友相关接口")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/check")
    @ApiOperation("批量判断是否是自己好友")
    public ApiResult<FriendCheckResp> check(@Valid FriendCheckReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.check(uid, request));
    }


    @PostMapping("/apply")
    @ApiOperation("申请好友")
    public ApiResult<Void> apply(@Valid @RequestBody FriendApplyReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.apply(uid, request);
        return ApiResult.success();
    }

    @DeleteMapping()
    @ApiOperation("删除好友")
    public ApiResult<Void> delete(@Valid @RequestBody FriendDeleteReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.deleteFriend(uid, request.getTargetUid());
        return ApiResult.success();
    }

    @GetMapping("/apply/page")
    @ApiOperation("好友申请列表")
    public ApiResult<PageBaseResp<FriendApplyResp>> page(@Valid PageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.pageApplyFriend(uid, request));
    }


    @GetMapping("/apply/unread")
    @ApiOperation("申请未读数")
    public ApiResult<FriendUnreadResp> unread() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.unread(uid));
    }



    @PutMapping("/apply")
    @ApiOperation("审批同意")
    public ApiResult<Void> applyApprove(@Valid @RequestBody FriendApproveReq request) {
        friendService.applyApprove(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    @GetMapping("/page")
    @ApiOperation("联系人列表")
    public ApiResult<CursorPageBaseResp<FriendResp>> friendList(@Valid CursorPageBaseReq request){
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.friendList(uid, request));
    }
}
