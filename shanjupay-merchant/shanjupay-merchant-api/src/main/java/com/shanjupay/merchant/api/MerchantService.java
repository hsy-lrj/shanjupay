package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
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

    /**
     * 资质申请接口
     * @param merchantId 商户的id
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    void applyMerchant(Long merchantId,MerchantDTO merchantDTO) throws BusinessException;

}
