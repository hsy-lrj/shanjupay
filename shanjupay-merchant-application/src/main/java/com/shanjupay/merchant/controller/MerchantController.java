package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.covert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class MerchantController {

    @org.apache.dubbo.config.annotation.Reference//注入远程调用的接口(dubbo)
    private MerchantService merchantService;

    @Resource//将本地的bean进行注入
    private SmsService smsService;

    /**
     * 获取手机验证码
     * @param phone
     * @return 返回的key
     */
    @ApiOperation("获取手机验证码")
    @GetMapping("/sms")
    public String getSmsCode(@RequestParam("phone") String phone) {
        //调用service进行发送手机验证码
        String key = smsService.sendMsg(phone);
        return key;
    }

    @ApiOperation("注册商户")
    @ApiImplicitParam(name = "merchantRegister", value = "注册信息", required = true, dataType = "MerchantRegisterVO", paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO){
        //校验验证码
        smsService.checkVerifiyCode(merchantRegisterVO.getVerifiykey(),merchantRegisterVO.getVerifiyCode());
        //注册商户(调用dubbo接口)
        //因为传递的对象不同，需要进行二次封装
//        MerchantDTO merchantDTO = new MerchantDTO();
//        merchantDTO.setUsername(merchantRegisterVO.getUsername());
//        merchantDTO.setMobile(merchantRegisterVO.getMobile());
        //使用对象转化器进行转化
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        //开始注册(向数据库中插入数据)
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }

    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

}
