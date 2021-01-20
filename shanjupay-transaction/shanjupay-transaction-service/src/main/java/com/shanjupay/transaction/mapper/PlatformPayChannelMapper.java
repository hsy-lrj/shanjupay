package com.shanjupay.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.entity.PlatformPayChannel;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author author
 * @since 2019-11-15
 */
@Repository
public interface PlatformPayChannelMapper extends BaseMapper<PlatformPayChannel> {

    /**
     * 根据平台服务类型查询原始支付渠道
     *
     * @param platformChannelCode
     * @return
     */
    @Select("select pc.* " +
            "from " +
            "pay_channel pay," +
            "platform_pay_channel pac, " +
            "platform_channel pc " +
            "where pay.CHANNEL_CODE = pac.PAY_CHANNEL " +
            "and pc.CHANNEL_CODE = pac.PLATFORM_CHANNEL " +
            "And pc.CHANNEL_CODE = #{platformChannelCode}")
    public List<PayChannelDTO> selectPayChannelByPlatformChannel(String platformChannelCode);

}
