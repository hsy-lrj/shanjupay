package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantCovert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;

    /**
     * 根据ID查询详细信息
     *
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(merchant.getId());
        merchantDTO.setMerchantName(merchant.getMerchantName());
        //设置其它属性...
        return merchantDTO;
    }

    /**
     * 商户注册服务接口
     *
     * @param merchantDTO 商户注册信息
     * @return 商户注册成功后的商户信息
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
        //使用对象转化器进行转化
        Merchant merchant = MerchantCovert.INSTANCE.merchantDTO2merchant(merchantDTO);
        merchantMapper.insert(merchant);
        //向dto中设置新增商户的id
//        merchantDTO.setId(merchant.getId());

        return MerchantCovert.INSTANCE.merchant2merchantDTO(merchant);
    }

    /**
     * 资质申请接口
     *
     * @param merchantId  商户的id
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    @Override
    @Transactional//添加事务
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        if (merchantId == null || merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据merchantId查询merchant，检验merchantId的合法性
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {//如果查询不到则认为是非法的
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //因为更新的对象与传入的对象不同，所以需要进行转化
        Merchant entity = MerchantCovert.INSTANCE.merchantDTO2merchant(merchantDTO);
        //将必要的参数设置到entity中
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile());//因为资质申请的手机号不能改，所以用数据库中原来的手机号
        entity.setAuditStatus("1");//设置审核状态为1（表示已申请待审核）
        entity.setTenantId(merchant.getTenantId());//租户id需要在接入sash时进行更新，所以还使用原来的信息
        //调用mapper进行更新
        merchantMapper.updateById(entity);
    }
}
