package com.th.mallchat.common.websocket.service;

import com.th.mallchat.common.websocket.domain.vo.response.WSBaseResp;
import io.netty.channel.Channel;
import me.chanjar.weixin.common.error.WxErrorException;

public interface WebSocketService {

    void handleLoginReq(Channel channel) throws WxErrorException;

    void connect(Channel channel);

    void remove(Channel channel);

    void scanLoginSuccess(Integer code, Long id);

    void waitAuthorize(Integer code);

    void authorize(Channel channel, String data);

    void sendToAllOnline(WSBaseResp<?> resp, Long uid);
}
