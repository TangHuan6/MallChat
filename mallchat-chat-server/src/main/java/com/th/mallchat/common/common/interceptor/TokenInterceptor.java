package com.th.mallchat.common.common.interceptor;

import cn.hutool.http.ContentType;
import com.google.common.base.Charsets;
import com.th.mallchat.common.common.domain.vo.response.ApiResult;
import com.th.mallchat.common.common.exception.HttpErrorEnum;
import com.th.mallchat.common.user.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_SCHEMA = "Bearer ";
    public static final String ATTRIBUTE_UID = "uid";

    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getToken(request);
        Long validUid = loginService.getValidUid(token);
        if (Objects.nonNull(validUid)){
            request.setAttribute(ATTRIBUTE_UID, validUid);
        }else {
            boolean publicURI = isPublicURI(request);
            if (!publicURI) {
                HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
            }
        }
        return true;
    }

    private boolean isPublicURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String[] split = requestURI.split("/");
        return split.length > 3 && "public".equals(split[3]);
    }

    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        String token = Optional.ofNullable(authorization)
                .filter(a -> a.startsWith(AUTHORIZATION_SCHEMA))
                .map(a -> a.replaceFirst(AUTHORIZATION_SCHEMA, ""))
                .orElse(null);
        return token;
    }
}
