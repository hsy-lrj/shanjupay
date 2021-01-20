package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import com.shanjupay.transaction.mapper.PlatformPayChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@org.apache.dubbo.config.annotation.Service
public class PayChannelServiceImpl implements PayChannelService {

    @Autowired
    private PlatformChannelMapper platformChannelMapper;
    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;
    @Autowired
    private PlatformPayChannelMapper platformPayChannelMapper;
    @Autowired
    private PayChannelParamMapper payChannelParamMapper;
    @Resource
    private Cache cache;

    /**
     * 获取平台服务类型
     *
     * @return
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        //查询platform_channel表中的全部记录
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        //将platformChannels转化成包含dto的list
        return PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
    }

    /**
     * 为app绑定平台服务类型
     *
     * @param appId                应用id
     * @param platformChannelCodes 平台服务类型列表
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
        //先根据应用id和服务类型查询是否已经绑定
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(
                new LambdaQueryWrapper<AppPlatformChannel>()
                        .eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        //如果没有查询到则证明没有绑定
        if (appPlatformChannel == null) {
            //创建一个entity对象用于插入数据
            appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);//应用id
            appPlatformChannel.setPlatformChannel(platformChannelCodes);//服务类型
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
    }

    /**
     * 根据应用id和平台服务类型列表查询应用是否已经绑定了某个服务类型
     *
     * @param appId           应用id
     * @param platformChannel 平台服务类型列表
     * @return 已绑定返回1，否则 返回0
     */
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(
                new LambdaQueryWrapper<AppPlatformChannel>()
                        .eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        //如果绑定返回1，没有绑定返回0
        if (appPlatformChannel != null) {
            return 1;
        }
        return 0;
    }

    /**
     * 根据服务类型查询支付渠道
     *
     * @param platformChannelCode 服务类型编码
     * @return 支付渠道列表
     * @throws BusinessException
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        return platformPayChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    /**
     * 支付渠道参数配置
     *
     * @param payChannelParamDTO 配置字符渠道的参数：商户id、应用id、服务类型code、
     *                           支付渠道code、配置名称、配置参数(前端将传递的数据转化为json)
     * @throws BusinessException
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException {
        //非空校验
        if (payChannelParamDTO == null || payChannelParamDTO.getChannelName() == null ||
                payChannelParamDTO.getParam() == null || payChannelParamDTO.getAppId() == null ||
                payChannelParamDTO.getPlatformChannelCode() == null || payChannelParamDTO.getPayChannel() == null
        ) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据应用id、平台服务类型、支付渠道查询记录
        //根据应用id、服务类型查询应用与服务类型的绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
        if (appPlatformChannelId == null) {
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        /**
         * 根据应用与服务类型的绑定id、支付渠道查询PayChannelParam的一条记录
         * pay_channel_param表的唯一字段：
         *                              PAY_CHANNEL:原始支付渠道编码
         *                              APP_PLATFORM_CHANNEL_ID:应用所选择的平台支付渠道ID
         */
        PayChannelParam entity = payChannelParamMapper.selectOne(
                new LambdaQueryWrapper<PayChannelParam>()
                        .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                        .eq(PayChannelParam::getPayChannel, payChannelParamDTO.getPayChannel()));
        if (entity != null) {//如果存在配置则更新
            entity.setChannelName(payChannelParamDTO.getChannelName());//配置名称
            entity.setParam(payChannelParamDTO.getParam());//支付参数
            payChannelParamMapper.updateById(entity);
        } else {//如果不存在则添加
            PayChannelParam entityNew = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            entityNew.setId(null);
            entityNew.setAppPlatformChannelId(appPlatformChannelId);//应用与服务类型绑定关系的id
            payChannelParamMapper.insert(entityNew);
        }
        //保存到redis中
        updateCache(payChannelParamDTO.getAppId(),payChannelParamDTO.getPlatformChannelCode());
    }

    /**
     * 根据应用id、服务类型查询支付渠道的参数列表
     *
     * @param appId           应用id
     * @param platformChannel 服务类型
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {
        /**
         * 从redis中获取.如果存在就返回
         */
        //获取存储的对应的key
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //根据key查询redis中是否保存了数据
        Boolean exists = cache.exists(redisKey);
        //判断redis中是否有记录
        if (exists){
            //如果redis中存在则从redis中获取出值
            String PayChannelParamDTO_String = cache.get(redisKey);
            //将取出的String值转化成Json
            List<PayChannelParamDTO> payChannelParamDTOS = JSON.parseArray(PayChannelParamDTO_String, PayChannelParamDTO.class);
            return payChannelParamDTOS;
        }
        /**
         * 从redis中获取.如果不存在就查询数据库，并将查询的结果保存到redis中
         */
        //根据应用id、服务类型查询应用与服务类型的绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        //判断查询应用与服务类型的绑定id是否为空
        if (appPlatformChannelId != null) {
            //通过应用与服务类型的绑定id查询支付渠道的参数列表
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(
                    new LambdaQueryWrapper<PayChannelParam>()
                            .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
            //使用对象转化工具类进行转化
            List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
            //保存到redis中
            updateCache(appId,platformChannel);
            return payChannelParamDTOS;
        }
        return null;
    }

    /**
     * 根据应用id、服务类型、支付渠道代码查询该支付渠道的参数配置信息
     *
     * @param appId           应用id
     * @param platformChannel 服务类型code
     * @param payChannel      支付渠道代码
     * @return
     * @throws BusinessException
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        //根据应用id、服务类型查询应用与服务类型的绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        //判断查询应用与服务类型的绑定id是否为空
        if (appPlatformChannelId != null) {
            //通过应用与服务类型的绑定id查询支付渠道的参数列表
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(
                    new LambdaQueryWrapper<PayChannelParam>()
                            .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
            //循环遍历支付渠道的参数列表
            for (PayChannelParam payChannelParam : payChannelParams) {
                //根据传递的支付渠道代码判断对应的支付渠道参数
                if (payChannelParam.getPayChannel().equals(payChannel)) {
                    //使用对象转化工具类进行转化
                    PayChannelParamDTO payChannelParamDTO = PayChannelParamConvert.INSTANCE.entity2dto(payChannelParam);
                    return payChannelParamDTO;
                }
            }

        }
        return null;
    }

    /**
     * 根据应用id、服务类型查询应用与服务类型的绑定id
     *
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIdByAppPlatformChannel(String appId, String platformChannelCode) {
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(
                new LambdaQueryWrapper<AppPlatformChannel>()
                        .eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if (appPlatformChannel != null) {
            return appPlatformChannel.getId();
        }
        return null;
    }

    /**
     * 根据应用id和服务类型code将查询到的支付渠道参数配置列表写入redis
     * @param appId 应用id
     * @param platformChannel 服务类型code
     */
    private void updateCache(String appId,String platformChannel) {
        /**
         * 构造redis的key
         *         key的格式：SJ_PAY_PARAM:appId:platformChannel
         */
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //根据key查询redis中是否保存了数据
        Boolean exists = cache.exists(redisKey);
        //判断redis中是否有记录
        if (exists){
            cache.del(redisKey);//如果存在就将其删除，重新插入
        }
        //根据应用id和服务类型code查询支付渠道参数
        //根据应用和服务类型找到它们绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        if(appPlatformChannelId != null){
            //应用和服务类型绑定id查询支付渠道参数记录
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
            List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
            //将payChannelParamDTOS转成json串存入redis
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }
    }

}
