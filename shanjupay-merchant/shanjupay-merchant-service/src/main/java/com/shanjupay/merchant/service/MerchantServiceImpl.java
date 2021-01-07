package com.shanjupay.merchant.service;

import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantCovert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;

    /**
     * 根据ID查询详细信息
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
     *商户注册服务接口
     * @param merchantDTO 商户注册信息
     * @return 商户注册成功后的商户信息
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
        //调用mapper向数据库插入数据
//        Merchant merchant = new Merchant();
//        merchant.setMobile(merchantDTO.getMobile());
//        //设置审核状态为0，表示为进行资质申请
//        merchantDTO.setAuditStatus("0");
        //使用对象转化器进行转化
        Merchant merchant = MerchantCovert.INSTANCE.merchantDTO2merchant(merchantDTO);
        merchantMapper.insert(merchant);
        //向dto中设置新增商户的id
//        merchantDTO.setId(merchant.getId());

        return MerchantCovert.INSTANCE.merchant2merchantDTO(merchant);
    }
}
