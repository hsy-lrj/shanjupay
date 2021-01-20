package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;

import java.util.List;

/**
 * 支付渠道服务 管理平台支付渠道，原始支付渠道，以及相关配置
 */
public interface PayChannelService {

    /**
     * 获取平台服务类型
     *
     * @return
     */
    List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException;

    /**
     * 为app绑定平台服务类型
     *
     * @param appId                应用id
     * @param platformChannelCodes 平台服务类型列表
     * @throws BusinessException
     */
    void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException;

    /**
     * 应用是否已经绑定了某个服务类型
     *
     * @param appId
     * @param platformChannel
     * @return 已绑定返回1，否则 返回0
     */
    int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException;

    /**
     * 根据服务类型查询支付渠道
     *
     * @param platformChannelCode 服务类型编码
     * @return 支付渠道列表
     * @throws BusinessException
     */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException;

    /**
     * 支付渠道参数配置
     *
     * @param payChannelParamDTO 配置字符渠道的参数：商户id、应用id、服务类型code、支付渠道code、配置名称、配置参数(前端将传递的数据转化为json)
     * @throws BusinessException
     */
    void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException;

    /**
     * 根据应用id、服务类型查询支付渠道的参数列表
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @return
     * @throws BusinessException
     */
    List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId,String platformChannel)throws BusinessException;

    /**
     * 根据应用id、服务类型、支付渠道代码查询该支付渠道的参数配置信息
     *
     * @param appId           应用id
     * @param platformChannel 服务类型code
     * @param payChannel      支付渠道代码
     * @return
     * @throws BusinessException
     */
   PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId,String platformChannel,String payChannel) throws BusinessException;
}
