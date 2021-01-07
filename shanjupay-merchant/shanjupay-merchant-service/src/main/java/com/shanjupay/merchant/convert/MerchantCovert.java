package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantCovert {
    MerchantCovert INSTANCE = Mappers.getMapper(MerchantCovert.class);

    //将entity的merchant对象转化为DTO对象
    MerchantDTO merchant2merchantDTO(Merchant merchant);

    //将DTO对象转化为entity的merchant对象
    Merchant merchantDTO2merchant(MerchantDTO merchantDTO);
}
