package com.shanjupay.merchant.service.impl;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service//注入到spring容器中
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Autowired
    RestTemplate restTemplate;

    //从Nacos配置文件中获取发送验证码的url地址
    @Value("${sms.url}")
    private String smsUrl;

    //从Nacos配置文件中获取验证码的有效期
    @Value("${sms.effectiveTime}")
    private String effectiveTime;


    /**
     * 请求服务发送手机验证码
     *
     * @param phone 手机号
     * @return 返回的key
     */
    @Override
    public String sendMsg(String phone) {
        //向验证码服务发送的请求地址
        String url = smsUrl + "/generate?name=sms&effectiveTime=" + effectiveTime;//验证码过期时间为600秒
        log.info("调用短信微服务发送验证码：url:{}", url);
        //请求体
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("mobile", phone);
        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //指定ContentType为APPLICATION_JSON
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //封装请求参数 (传入请求头和请求体)
        HttpEntity entity = new HttpEntity(body, httpHeaders);
        Map responseMap;
        try {
            /**
             * 开始发送post请求
             * url ：请求的路径
             * HttpMethod.POST ：请求的类型
             * entity ：请求的参数
             * Map.class ：返回值的类型
             */
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, entity,
                    Map.class);
            //日志信息
            log.info("调用短信微服务发送验证码: 返回值:{}", JSON.toJSONString(exchange));
            //获取到返回的信息
            responseMap = exchange.getBody();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
//            throw new RuntimeException("发送验证码出错");
            throw new BusinessException(CommonErrorCode.E_100102);//改为抛出自定义异常
        }

        if (responseMap == null || responseMap.get("result") == null) {//判断是否存在返回的信息
//            throw new RuntimeException("发送验证码出错");
            throw new BusinessException(CommonErrorCode.E_100102);//改为抛出自定义异常
        }
        /**
         * 返回的json类型：
         *  {
         *      "code": 0,
         *      "msg": "string",
         *      "result": {
         *          "content": "string",
         *          "key": "string"
         *      }
         *  }
         *
         */
        //获取result集合
        Map resultMap = (Map) responseMap.get("result");
        //获取key并转化成String
        String key = resultMap.get("key").toString();
        return key;

    }

    /**
     * 校验验证码
     *
     * @param verifiyKey  发送验证码后返回的key（唯一标识）
     * @param verifiyCode 验证码
     */
    @Override
    public void checkVerifiyCode(String verifiyKey, String verifiyCode) {
        //向校验验证码服务发送请求的地址
        String url = smsUrl + "/verify?name=sms&verificationCode=" + verifiyCode + "&verificationKey=" + verifiyKey;
        Map responseMap = null;
        try {
            /**
             * 开始发送post请求
             * url ：请求的路径
             * HttpMethod.POST ：请求的类型
             * HttpEntity.EMPTY ：请求的参数为空
             * Map.class ：返回值的类型
             */
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST,HttpEntity.EMPTY, Map.class);
            //日志信息
            log.info("调用短信微服务发送验证码: 返回值:{}", JSON.toJSONString(exchange));
            //获取到返回的信息
            /**
             * 返回的json类型：
             *      {
             *           "code": 0,
             *           "msg": "正常",
             *           "result": true
             *      }
             */
            responseMap = exchange.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage(), e);
//            throw new RuntimeException("验证码错误");
            throw new BusinessException(CommonErrorCode.E_100102);//改为抛出自定义异常
        }
        if (responseMap == null || responseMap.get("result") == null || !(Boolean) responseMap.get("result")) {
//            throw new RuntimeException("验证码错误");
            throw new BusinessException(CommonErrorCode.E_100102);//改为抛出自定义异常
        }

    }
}
