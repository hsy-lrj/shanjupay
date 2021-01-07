package com.shanjupay.merchant.covert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantRegisterConvert {
    MerchantRegisterConvert INSTANCE = Mappers.getMapper(MerchantRegisterConvert.class);

    //将vo对象转化为DTO对象
    MerchantDTO vo2dto(MerchantRegisterVO vo);

    //将DTO对象转化为vo对象
    MerchantRegisterVO dto2vo(MerchantDTO dto);
}
