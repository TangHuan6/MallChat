package com.th.mallchat.common.user.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.dto.IpResult;
import com.th.mallchat.common.user.domain.entity.IpDetail;
import com.th.mallchat.common.user.domain.entity.IpInfo;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.service.IpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
/**
 * 首先是获取ip
 */
public class IpServiceImpl implements IpService, DisposableBean {
    private static ExecutorService EXECUTOR = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(500), new NamedThreadFactory("refresh-ipDetail", false));


    @Autowired
    private UserDao userDao;

    @Override
    public void refreshIpDetailAsync(Long uid) {
        EXECUTOR.execute(() -> {
            User user = userDao.getById(uid);
            IpInfo ipInfo = user.getIpInfo();
            if (Objects.isNull(ipInfo)){
                return;
            }
            //判断ip详情是否需要刷新 如果当前用户的 updateIp 和 updateDetail的Ip相同返回null不需要更新 如果不相同 返回需要更新的Ip
            String ip = ipInfo.needRefreshIp();
            if (Objects.isNull(ip)){
                return;
            }
            //尝试去获取新ip的 ip详情
            IpDetail ipDetail = TryGetIpDetailOrNullTreeTimes(ip);
            if (Objects.nonNull(ipDetail)){
                ipInfo.refreshIpDetail(ipDetail);
                User update = new User();
                update.setId(uid);
                update.setIpInfo(ipInfo);
                userDao.updateById(update);
            }else {
                log.error("get ip detail fail ip:{},uid:{}", ip, uid);
            }
        });
    }

    private IpDetail TryGetIpDetailOrNullTreeTimes(String ip) {
        for (int i = 0; i < 3; i++) {
            IpDetail ipDetail = getIpDetailOrNull(ip);
            if (Objects.nonNull(ipDetail)){
                return ipDetail;
            }
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    private static IpDetail getIpDetailOrNull(String ip) {
        String body = HttpUtil.get("https://ip.taobao.com/outGetIpInfo?ip=" + ip + "&accessKey=alibaba-inc");
        try {
            IpResult<IpDetail> result = JSONUtil.toBean(body, new TypeReference<IpResult<IpDetail>>() {
            }, false);
            if (result.isSuccess()) {
                return result.getData();
            }
        }catch (Exception ignored){

        }
        return null;
    }

    public static void main(String[] args) {
        IpDetail ipDetailOrNull = getIpDetailOrNull("113.204.50.117");
        System.out.println(ipDetailOrNull);
    }

    @Override
    public void destroy() throws Exception {
        EXECUTOR.shutdown();
        if (!EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {//最多等30秒，处理不完就拉倒
            if (log.isErrorEnabled()) {
                log.error("Timed out while waiting for executor [{}] to terminate", EXECUTOR);
            }
        }
    }
}
