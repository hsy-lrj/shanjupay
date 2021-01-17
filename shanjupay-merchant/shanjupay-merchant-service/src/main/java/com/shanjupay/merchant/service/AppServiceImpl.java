package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppCovert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@org.apache.dubbo.config.annotation.Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AppMapper appMapper;
    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 创建应用
     *
     * @param merchantId 商户id
     * @param appDTO        应用的信息
     * @return 创建成功的应用信息
     * @throws BusinessException
     */
    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {

        //判断传入的参数和对象是否为空
        if (merchantId == null || appDTO == null || StringUtil.isBlank(appDTO.getAppName())) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {//商户不存在
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())) {//商户还未通过认证审核，不能创建应用
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        //应用名称需要校验唯一性
        //传入的应用名称
        String appName = appDTO.getAppName();
        //调用方法进行查询并判断
        if (isExistAppName(appName)) {
            throw new BusinessException(CommonErrorCode.E_200004);
        }
        //生成应用ID,使用UUID方式生成。
        String appId = UUID.randomUUID().toString();
        appDTO.setAppId(appId);//应用id
        appDTO.setMerchantId(merchantId);//商户id
        //将DTO对象转化成entity对象
        App entity = AppCovert.INSTANCE.dto2entity(appDTO);
        //保存数据
        appMapper.insert(entity);
        //因为返回AppDTO，所以需要转化
        return AppCovert.INSTANCE.entity2dto(entity);
    }

    /**
     * 根据商户id查询应用
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {
        List<App> apps = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        List<AppDTO> appDTOS = AppCovert.INSTANCE.listentity2dto(apps);
        return appDTOS;
    }

    /**
     * 根据应用id查询详细信息
     * @param id
     * @return
     * @throws BusinessException
     */
    @Override
    public AppDTO getAppById(String id) throws BusinessException {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>().eq(App::getAppId, id));
        AppDTO appDTO = AppCovert.INSTANCE.entity2dto(app);
        return appDTO;
    }

    /**
     * 校验应用名是否已被使用
     *
     * @param appName
     * @return
     */
    public Boolean isExistAppName(String appName) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("APP_NAME", appName);
        Integer count = appMapper.selectCount(queryWrapper);
        return count.intValue() > 0;
    }

}
