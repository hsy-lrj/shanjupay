package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-01-07T20:20:29+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 1.8.0_101 (Oracle Corporation)"
)
public class MerchantCovertImpl implements MerchantCovert {

    @Override
    public MerchantDTO merchant2merchantDTO(Merchant merchant) {
        if ( merchant == null ) {
            return null;
        }

        MerchantDTO merchantDTO = new MerchantDTO();

        merchantDTO.setId( merchant.getId() );
        merchantDTO.setMerchantName( merchant.getMerchantName() );
        merchantDTO.setMerchantNo( merchant.getMerchantNo() );
        merchantDTO.setMerchantAddress( merchant.getMerchantAddress() );
        merchantDTO.setMerchantType( merchant.getMerchantType() );
        merchantDTO.setBusinessLicensesImg( merchant.getBusinessLicensesImg() );
        merchantDTO.setIdCardFrontImg( merchant.getIdCardFrontImg() );
        merchantDTO.setIdCardAfterImg( merchant.getIdCardAfterImg() );
        merchantDTO.setUsername( merchant.getUsername() );
        merchantDTO.setMobile( merchant.getMobile() );
        merchantDTO.setContactsAddress( merchant.getContactsAddress() );
        merchantDTO.setAuditStatus( merchant.getAuditStatus() );
        merchantDTO.setTenantId( merchant.getTenantId() );

        return merchantDTO;
    }

    @Override
    public Merchant merchantDTO2merchant(MerchantDTO merchantDTO) {
        if ( merchantDTO == null ) {
            return null;
        }

        Merchant merchant = new Merchant();

        merchant.setId( merchantDTO.getId() );
        merchant.setMerchantName( merchantDTO.getMerchantName() );
        merchant.setMerchantNo( merchantDTO.getMerchantNo() );
        merchant.setMerchantAddress( merchantDTO.getMerchantAddress() );
        merchant.setMerchantType( merchantDTO.getMerchantType() );
        merchant.setBusinessLicensesImg( merchantDTO.getBusinessLicensesImg() );
        merchant.setIdCardFrontImg( merchantDTO.getIdCardFrontImg() );
        merchant.setIdCardAfterImg( merchantDTO.getIdCardAfterImg() );
        merchant.setUsername( merchantDTO.getUsername() );
        merchant.setMobile( merchantDTO.getMobile() );
        merchant.setContactsAddress( merchantDTO.getContactsAddress() );
        merchant.setAuditStatus( merchantDTO.getAuditStatus() );
        merchant.setTenantId( merchantDTO.getTenantId() );

        return merchant;
    }
}
