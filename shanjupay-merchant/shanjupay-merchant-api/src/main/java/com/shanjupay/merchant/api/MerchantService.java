package com.shanjupay.merchant.api;

import com.shanjupay.merchant.api.dto.MerchantDTO;

public interface MerchantService {
    /**
     * 根据ID查询详细信息
     * @param merchantId
     */
    MerchantDTO queryMerchantById(Long merchantId);

    /**
     *商户注册服务接口
     * @param merchantDTO 商户注册信息
     * @return 商户注册成功后的商户信息
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO);

}
