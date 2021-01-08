package com.shanjupay.common.domain;

/**
 * 自定义业务异常处理器
 */
public class RestErrorResponse {
    //错误状态码
    private String errCode;
    //错误信息
    private String errMessage;

    public RestErrorResponse(String errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

}
