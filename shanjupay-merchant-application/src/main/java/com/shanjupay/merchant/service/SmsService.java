package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

public interface SmsService {
    /**
     * 请求服务发送手机验证码
     *
     * @param phone 手机号
     * @return 返回的key
     */
    String sendMsg(String phone) throws BusinessException;

    ;

    /**
     * 校验验证码
     *
     * @param verifiyKey  发送验证码后返回的key（唯一标识）
     * @param verifiyCode 验证码
     */
    void checkVerifiyCode(String verifiyKey, String verifiyCode) throws BusinessException;
}
