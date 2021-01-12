package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.vo.MerchantRegisterVO;

import java.util.Map;

public interface SmsService {

    /**
     * 发送短信的方法
     * @param param  随机生成的验证码
     * @param phone  要发送的手机号
     * @return
     */
    boolean send(Map<String, Object> param, String phone);

    /**
     * 校验验证码
     * @param merchantRegisterVO 用户传入的数据
     */
    void checkVerifiyCode(MerchantRegisterVO merchantRegisterVO) throws BusinessException;
}
