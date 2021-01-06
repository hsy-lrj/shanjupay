package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.service.SmsService;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

}
