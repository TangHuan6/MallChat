package com.th.mallchat.common.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.th.mallchat.common.common.constant.RedisKey;
import com.th.mallchat.common.common.utils.JwtUtils;
import com.th.mallchat.common.common.utils.RedisUtils;
import com.th.mallchat.common.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    public static final int TOKEN_EXPIRE_DAYS = 3;
    private static final int TOKEN_RENEWAL_DAYS = 1;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean verify(String token) {
        return false;
    }

    @Override
    public void renewalTokenIfNecessary(String token) {
        Long uid = getValidUid(token);
        String userTokenKey = getUserTokenKey(uid);
        Long expireDays = RedisUtils.getExpire(userTokenKey, TimeUnit.DAYS);
        if (expireDays == -2) {//不存在的key
            return;
        }
        if (expireDays < TOKEN_RENEWAL_DAYS) {//小于一天的token帮忙续期
            RedisUtils.expire(userTokenKey, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        }
    }

    @Override
    public String login(Long uid) {
        String token = jwtUtils.createToken(uid);
        RedisUtils.set(getUserTokenKey(uid),token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        return token;
    }

    /**
     * 假设你在两个设备 A、B 登录了同一个账号。
     *A 先登录，得到 tokenA，后端把它存进 Redis。
     *B 后登录，得到 tokenB，Redis 被覆盖，tokenB 成为最新 token。
     *A 再请求接口，带上的是 tokenA。
     *此时：
     *jwtUtils.getUidOrNull(tokenA) 能正常解析出 uid。
     *但 getUserToken(uid) 查出的 token 是 tokenB，不等于 tokenA。
     *所以返回 null，表示 tokenA 无效了。
     *这就是“只允许一个地方登录”的登录控制方式。
     * @param token
     * @return
     */
    @Override
    public Long getValidUid(String token) {
        Long uid = jwtUtils.getUidOrNull(token);
        if (Objects.isNull(uid)) {
            return null;
        }
        String oldToken = getUserTokenKey(uid);
        return Objects.equals(oldToken, token) ? uid : null;
    }

    private String getUserTokenKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_TOKEN_STRING,uid);
    }
}
