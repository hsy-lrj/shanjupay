package com.shanjupay.merchant.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service//注入到spring容器中
@Slf4j
public class SmsServiceImpl implements SmsService {

    //注入redis
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    /**
     * 从Nacos的merchant-application.yaml配置文件中获取
     */
    @Value("${oss.aliyun.AccessKeyId}")
    private String accessKeyId;
    @Value("${oss.aliyun.AccessKeySecret}")
    private String accessKeySecret;
    @Value("${oss.aliyun.SignName}")
    private String signName;
    @Value("${oss.aliyun.TemplateCode}")
    private String templateCode;

    //发送短信的方法
    @Override
    public boolean send(Map<String, Object> param, String phone) {
        if(StringUtils.isEmpty(phone)) return false;

        DefaultProfile profile =
                DefaultProfile.getProfile("default", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        //设置相关固定的参数
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);//提交方式
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        //设置发送相关的参数
        request.putQueryParameter("PhoneNumbers",phone); //设置要发送的手机号
        request.putQueryParameter("SignName",signName); //申请阿里云的签名名称
        request.putQueryParameter("TemplateCode",templateCode); //申请阿里云的模板code
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param)); //验证码数据，需要转换json数据进行传递

        try {
            //进行发送
            CommonResponse response = client.getCommonResponse(request);
            //返回boolean类型为发送是否成功
            return response.getHttpResponse().isSuccess();
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 校验验证码
     * @param merchantRegisterVO 用户传入的数据
     */
    @Override
    public void checkVerifiyCode(MerchantRegisterVO merchantRegisterVO) {
        //获取redis中保存的验证码(key是手机号)
        String code = redisTemplate.opsForValue().get(merchantRegisterVO.getMobile());
        //获取用户输入的验证码
        String verifiyCode = merchantRegisterVO.getVerifiyCode();
        if (!code.equalsIgnoreCase(verifiyCode)){
            throw new BusinessException(CommonErrorCode.E_100107);
        }
    }
}
