package com.shanjupay.merchant.common.intercept;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.domain.RestErrorResponse;
import oracle.jrockit.jfr.VMJFR;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理
 */
@ControllerAdvice//与@ExceptionHandler注解配合使用实现全局异常处理
public class GlobalExceptionHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ResponseBody//将返回的数据转化为JSON
    @ExceptionHandler(value = Exception.class)//用于捕获Exception异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//当出现错误，返回500的状态
    public RestErrorResponse processExcetion(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Exception e) {
        /**
         * 解析异常信息：
         *      如果是自定义异常，则直接取出异常信息并返回
         *      如果不是自定义异常，则统一定义为99999系统未知错误
         */
        if (e instanceof BusinessException) {//则直接取出异常信息并返回
            //在日志中输出异常信息
            LOGGER.info(e.getMessage(), e);
            //将捕获到的异常转化为自定义异常
            BusinessException businessException = (BusinessException) e;
            //取出异常的信息
            ErrorCode errorCode = businessException.getErrorCode();
            //获取错误的状态码
            int code = errorCode.getCode();
            //获取错误的信息
            String desc = errorCode.getDesc();
            //将错误返回
            return new RestErrorResponse(desc, String.valueOf(code));
        }else {//则统一定义为99999系统未知错误
            //在日志中输出异常信息
            LOGGER.error("系统异常：", e);
            //返回错误
            return new RestErrorResponse(CommonErrorCode.UNKNOWN.getDesc(), String.valueOf(CommonErrorCode.UNKNOWN.getCode()));
        }


    }

}
