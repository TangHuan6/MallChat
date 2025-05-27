package com.th.mallchat.common.websocket.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.th.mallchat.common.common.config.ThreadPoolConfig;
import com.th.mallchat.common.common.event.UserOnlineEvent;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.entity.IpInfo;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.enums.RoleEnum;
import com.th.mallchat.common.user.service.LoginService;
import com.th.mallchat.common.user.service.RoleService;
import com.th.mallchat.common.websocket.domain.dto.WSChannelExtraDTO;
import com.th.mallchat.common.websocket.domain.enums.WSRespTypeEnum;
import com.th.mallchat.common.websocket.domain.vo.response.WSBaseResp;
import com.th.mallchat.common.websocket.domain.vo.response.WSLoginUrl;
import com.th.mallchat.common.websocket.service.WebSocketService;
import com.th.mallchat.common.websocket.service.adapter.WebSocketAdapter;
import com.th.mallchat.common.websocket.utils.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *管理WebSocket逻辑
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    @Lazy
    private WxMpService wxMpService;

    public static final int MAXIMUM_SIZE = 1000;
    public static final Duration DURATION = Duration.ofHours(1);
    /**
     * 所有已经连接的websocket连接列表
     */
    private static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();
    /**
     * 临时保存登录Code和channel的映射关系
     */
    private static final Cache<Integer,Channel> WAIT_LOGIN_MAP = Caffeine.newBuilder()
            .maximumSize(MAXIMUM_SIZE)
            .expireAfterWrite(DURATION)
            .build();
    @Autowired
    private UserDao userDao;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RoleService roleService;

    @Autowired
    @Qualifier(ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;



    @Override
    public void handleLoginReq(Channel channel) throws WxErrorException {
        //生成随机不重复的登录码,并将channel存在本地cache中
        Integer code = generateLoginCode(channel);
        //找微信申请带参二维码
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(code, (int) DURATION.getSeconds());
        //推送给前端
        WSBaseResp<WSLoginUrl> wsBaseResp = WebSocketAdapter.buildLoginResp(wxMpQrCodeTicket);
        sendMsg(channel,wsBaseResp);
    }

    private void sendMsg(Channel channel, WSBaseResp<?> resp) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(resp)));
    }

    /**
     * 获取不重复的登录的code，微信要求最大不超过int的存储极限
     */
    private Integer generateLoginCode(Channel channel) {
        Integer code;
        do {
            code = RandomUtil.randomInt(Integer.MAX_VALUE);
        }while (Objects.nonNull(WAIT_LOGIN_MAP.asMap().putIfAbsent(code,channel)));
        return code;
    }

    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel, new WSChannelExtraDTO());
    }

    @Override
    public void remove(Channel channel) {
        ONLINE_WS_MAP.remove(channel);
        //todo 用户下线
    }

    @Override
    public void scanLoginSuccess(Integer code, Long id) {
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if (Objects.isNull(channel)) {
            return;
        }
        User user = userDao.getById(id);
        //获取token 移除code
        WAIT_LOGIN_MAP.invalidate(code);

        String token = loginService.login(id);

        loginSuccess(channel,user,token);

    }

    @Override
    public void waitAuthorize(Integer code) {
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if (Objects.isNull(channel)){
            return;
        }
        sendMsg(channel,WebSocketAdapter.buildScanSuccessResp());
    }

    @Override
    public void authorize(Channel channel, String token) {
        Long validUid = loginService.getValidUid(token);
        if (Objects.nonNull(validUid)) {
            User user = userDao.getById(validUid);
            loginSuccess(channel,user,token);
        }else {
            sendMsg(channel,WebSocketAdapter.buildInvalidTokenResp());
        }
    }

    @Override
    public void sendToAllOnline(WSBaseResp<?> resp, Long uid) {
        ONLINE_WS_MAP.forEach((channel,ext) -> {
            if (Objects.nonNull(uid) && Objects.equals(ext.getUid(),uid)){
                return;
            }
            threadPoolTaskExecutor.execute(() -> sendMsg(channel,resp));
        });
    }

    private void loginSuccess(Channel channel, User user, String token) {
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        wsChannelExtraDTO.setUid(user.getId());
        boolean hasPower = roleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER);
        sendMsg(channel,WebSocketAdapter.buildLoginSuccessResp(user,token,hasPower));
        //发送用户上线事件
        user.setLastOptTime(new Date());
        user.refreshIp(NettyUtil.getAttr(channel,NettyUtil.IP));
        applicationEventPublisher.publishEvent(new UserOnlineEvent(this,user));
    }


}
