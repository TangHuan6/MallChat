package com.th.mallchat.common.websocket.service.adapter;

import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.websocket.domain.enums.WSRespTypeEnum;
import com.th.mallchat.common.websocket.domain.vo.response.WSBaseResp;
import com.th.mallchat.common.websocket.domain.vo.response.WSLoginSuccess;
import com.th.mallchat.common.websocket.domain.vo.response.WSLoginUrl;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

public class WebSocketAdapter {
    public static WSBaseResp<WSLoginUrl> buildLoginResp(WxMpQrCodeTicket wxMpQrCodeTicket) {
        WSBaseResp<WSLoginUrl> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.LOGIN_URL.getType());
        wsBaseResp.setData(WSLoginUrl.builder().loginUrl(wxMpQrCodeTicket.getUrl()).build());
        return wsBaseResp;
    }

    public static WSBaseResp<?> buildLoginSuccessResp(User user, String token,boolean hasPower) {
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        WSLoginSuccess wsLoginSuccess = WSLoginSuccess.builder()
                .avatar(user.getAvatar())
                .name(user.getName())
                .token(token)
                .power(hasPower ? 1 : 0)
                .uid(user.getId())
                .build();
        resp.setData(wsLoginSuccess);
        return resp;
    }

    public static WSBaseResp buildScanSuccessResp() {
        WSBaseResp wsBaseResp = new WSBaseResp();
        wsBaseResp.setType(WSRespTypeEnum.LOGIN_SCAN_SUCCESS.getType());
        return wsBaseResp;
    }

    public static WSBaseResp<?> buildInvalidTokenResp() {
        WSBaseResp wsBaseResp = new WSBaseResp();
        wsBaseResp.setType(WSRespTypeEnum.INVALIDATE_TOKEN.getType());
        return wsBaseResp;
    }
}
