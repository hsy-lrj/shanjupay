package com.shanjupay.merchant.service;

public interface SmsService {
    /**
     * 请求服务发送手机验证码
     * @param phone 手机号
     * @return 返回的key
     */
    String sendMsg(String phone);
}
