package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

/**
 * 应用管理相关的接口
 */
public interface AppService {

    /**
     * 创建应用
     * @param merchantId 商户id
     * @param app 应用的信息
     * @return 创建成功的应用信息
     * @throws BusinessException
     */
    AppDTO createApp(Long merchantId, AppDTO app) throws BusinessException;

    /**
     * 根据商户id查询应用
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;

    /**
     * 根据应用id查询详细信息
     * @param id
     * @return
     * @throws BusinessException
     */
    AppDTO getAppById(String id) throws BusinessException;


}
