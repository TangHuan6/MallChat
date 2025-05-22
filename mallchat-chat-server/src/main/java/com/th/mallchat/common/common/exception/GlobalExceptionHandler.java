package com.th.mallchat.common.common.exception;

import com.th.mallchat.common.common.domain.vo.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResult<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringBuilder errormsg = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(x -> errormsg.append(x.getField()).append(x.getDefaultMessage()).append(","));
        String message = errormsg.toString();
        log.info("validation parameters error！The reason is:{}",message);
        return ApiResult.fail(CommonErrorEnum.PARAM_VALID.getErrorCode(),message.substring(0,message.length()-1));
    }

    /**
     * 自定义校验异常（如参数校验等）
     */
    @ExceptionHandler(value = BusinessException.class)
    public ApiResult businessExceptionHandler(BusinessException e) {
        log.info("business exception！The reason is：{}", e.getMessage(), e);
        return ApiResult.fail(e.getErrorCode(), e.getMessage());
    }
}
