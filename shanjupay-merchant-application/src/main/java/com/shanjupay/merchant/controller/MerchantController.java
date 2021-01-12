package com.shanjupay.merchant.controller;

import com.shanjupay.common.util.RandomUtil;
import com.shanjupay.common.util.SecurityUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.covert.MerchantDetailConvert;
import com.shanjupay.merchant.covert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class MerchantController {

    @org.apache.dubbo.config.annotation.Reference//注入远程调用的接口(dubbo)
    private MerchantService merchantService;
    @Resource//将本地的bean进行注入
    private SmsService smsService;
    @Resource//将本地的bean进行注入
    private FileService fileService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发送短信验证码(使用阿里云)
     */
    @ApiOperation("发送短信验证码")
    @GetMapping("/sms")
    public String sendSmsCode(@RequestParam("phone") String phone) {
        //1.从redis中获取验证码，如果获取到直接返回
        String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)) {
            return "验证码已经发送";
        }
        //2.如果redis获取不到，进行阿里云发送
        //随机生成验证码
        code = RandomUtil.getFourBitRandom();
        Map<String, Object> param = new HashMap<>();
        param.put("code", code);

        Boolean isSend = smsService.send(param, phone);
        if (isSend) {
            //发送成功，把成功的验证码放入到redis中，设置redis的有效时间
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return "发送成功";
        }
        return "发送失败";
    }

    /**
     * 商户注册
     *
     * @param merchantRegisterVO
     * @return
     */
    @ApiOperation("注册商户")
    @ApiImplicitParam(name = "merchantRegister", value = "注册信息", required = true, dataType = "MerchantRegisterVO", paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO) {
        //校验验证码(调用dubbo接口)
        smsService.checkVerifiyCode(merchantRegisterVO);
        //注册商户(调用dubbo接口)
        //因为传递的对象不同，需要进行二次封装,使用对象转化器进行转化
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        //开始注册(向数据库中插入数据)
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }

    /**
     * 证件上传
     *
     * @param file
     * @return
     */
    @ApiOperation("证件上传")
    @PostMapping("/upload")
    public String uploadOssFile(@ApiParam(value = "上传的文件", required = true)
                                @RequestParam("file") MultipartFile file) throws IOException, BatchUpdateException {
        //进行文件的上传，返回文件的访问路径
        return fileService.upload(file);
    }

    /**
     * 商户资质申请
     * @param merchantDetailVO 前端传递的信息
     */
    @ApiOperation("商户资质申请")
    @ApiImplicitParams({@ApiImplicitParam(name = "merchantInfo", value = "商户认证资料", required = true,
            dataType = "MerchantDetailVO", paramType = "body")
    })
    @PostMapping("/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantDetailVO) {
        //商户id(merchantId)是当前登录商户的id，需要先取出当前登录的商户
        //解析token得到商户id
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantDetailVO);
        //开始进行资质申请
        merchantService.applyMerchant(merchantId,merchantDTO);
    }

    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

}
