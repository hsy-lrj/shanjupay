package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(value = "商户平台‐应用管理", tags = "商户平台‐应用相关", description = "商户平台‐应用相关")
@RestController
public class AppController {

    @Reference//调用dubbo的服务
    private AppService appService;

    @ApiOperation("商户创建应用")
    @ApiImplicitParams({@ApiImplicitParam(name = "app", value = "应用信息", required = true, dataType = "AppDTO", paramType = "body")})
    @PostMapping(value = "/my/apps")
    public AppDTO createApp(@RequestBody AppDTO app) {
        //因为调用service需要商户的id，所以需要获取商户的id
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.createApp(merchantId, app);
    }

    /**
     * 查询商户下的应用列表
     *
     * @return
     */
    @ApiOperation("查询商户下的应用列表")
    @GetMapping(value = "/my/apps")
    public List<AppDTO> queryMyApps() {
        //先获取到商户id
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.queryAppByMerchant(merchantId);
    }

    /**
     * 根据appid获取应用的详细信息
     * @param appId
     * @return
     */
    @ApiOperation("根据appid获取应用的详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "商户应用id", required = true, dataType =
                    "String", paramType = "path")})
    @GetMapping(value = "/my/apps/{appId}")
    public AppDTO getApp(@PathVariable String appId) {
        return appService.getAppById(appId);
    }


}
