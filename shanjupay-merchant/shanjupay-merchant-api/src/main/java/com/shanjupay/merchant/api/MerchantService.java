package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;

public interface MerchantService {
    /**
     * 根据ID查询详细信息
     *
     * @param merchantId
     */
    MerchantDTO queryMerchantById(Long merchantId);

    /**
     * 商户注册服务接口
     *
     * @param merchantDTO 商户注册信息
     * @return 商户注册成功后的商户信息
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO);

    /**
     * 资质申请接口
     *
     * @param merchantId  商户的id
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 新增门店
     *
     * @param storeDTO 门店信息
     * @return 新增成功的门店信息
     * @throws BusinessException
     */
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;

    /**
     * 新增员工
     *
     * @param staffDTO 员工信息
     * @return 新增成功的元信息
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;

    /**
     * 将员工和门店进行绑定(设置管理员)
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId, Long staffId) throws BusinessException;

}
