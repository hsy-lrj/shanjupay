package com.shanjupay.merchant.service;

import com.alibaba.nacos.client.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantCovert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;
    @Autowired
    StoreMapper storeMapper;
    @Autowired
    StaffMapper staffMapper;
    @Autowired
    StoreStaffMapper storeStaffMapper;
    @Reference
    TenantService tenantService;

    /**
     * 根据ID查询详细信息
     *
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        //使用对象转化器进行转化
        MerchantDTO merchantDTO = MerchantCovert.INSTANCE.merchant2merchantDTO(merchant);
        return merchantDTO;
    }

    /**
     * 1、商户注册服务接口
     * 2、调用SaaS接口：新增租户、商户、绑定租户和商户的关系，初始化权限
     *
     * @param merchantDTO 商户注册信息
     * @return 商户注册成功后的商户信息
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
        //校验参数的合法性
        if (merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //校验手机号是否为空
        if (StringUtils.isBlank(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //校验密码是否为空
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //手机号格式校验
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //校验手机号的唯一性
        //根据手机号查询商户表，如果存在记录则说明手机号已存在
        Integer count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile()));
        if (count > 0) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        //调用SaaS接口
        /**
         * 构造调用的参数：
         *         1、手机号
         *         2、账号
         *         3、密码
         *         4、租户类型：shanju-merchant
         *         5、默认套餐：shanju-merchant
         *         6、租户名称，同账号名
         */
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");//租户类型
        createTenantRequestDTO.setBundleCode("shanju-merchant");//默认套餐,根据套餐进行分配权限
        createTenantRequestDTO.setName(merchantDTO.getUsername());//租户名称，同账号名
        //如果租户在SaaS已经存在，SaaS直接 返回此租户的信息，否则进行添加
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);
        //获取租户的id
        if (tenantAndAccount == null || tenantAndAccount.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_200012);
        }
        //租户id
        Long tenantId = tenantAndAccount.getId();
        //根据租户id从商户表查询，如果存在记录则不允许添加商户（租户id在商户表唯一）
        Integer count1 = merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId)
        );
        if (count1 > 0) {
            throw new BusinessException(CommonErrorCode.E_200017);
        }
        //使用对象转化器进行转化
        Merchant merchant = MerchantCovert.INSTANCE.merchantDTO2merchant(merchantDTO);
        //设置所对应的租户的Id
        merchant.setTenantId(tenantId);
        //审核状态为0-未进行资质申请
        merchant.setAuditStatus("0");
        //新增商户
        merchantMapper.insert(merchant);
        //新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setStoreName("根门店");
        storeDTO.setMerchantId(merchant.getId());//商户id
        storeDTO.setStoreStatus(true);//门店的状态
        StoreDTO store = createStore(storeDTO);
        //新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMobile(merchantDTO.getMobile());//手机号
        staffDTO.setUsername(merchantDTO.getUsername());//账号
        staffDTO.setStoreId(store.getId());//员所属门店id
        staffDTO.setMerchantId(merchant.getId());//商户id
        staffDTO.setStaffStatus(true);//员工的状态：启用
        StaffDTO staff = createStaff(staffDTO);
        //为门店设置管理员
        bindStaffToStore(store.getId(), staff.getId());
        return MerchantCovert.INSTANCE.merchant2merchantDTO(merchant);
    }

    /**
     * 资质申请接口
     *
     * @param merchantId  商户的id
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    @Override
    @Transactional//添加事务
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        if (merchantId == null || merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据merchantId查询merchant，检验merchantId的合法性
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {//如果查询不到则认为是非法的
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //因为更新的对象与传入的对象不同，所以需要进行转化
        Merchant entity = MerchantCovert.INSTANCE.merchantDTO2merchant(merchantDTO);
        //将必要的参数设置到entity中
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile());//因为资质申请的手机号不能改，所以用数据库中原来的手机号
        entity.setAuditStatus("1");//设置审核状态为1（表示已申请待审核）
        entity.setTenantId(merchant.getTenantId());//租户id需要在接入sash时进行更新，所以还使用原来的信息
        //调用mapper进行更新
        merchantMapper.updateById(entity);
    }

    /**
     * 新增门店
     *
     * @param storeDTO 门店信息
     * @return 新增成功的门店信息
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        //调用对象转化器进行转化
        Store entity = StoreConvert.INSTANCE.dto2entity(storeDTO);
        //将门店信息插入到数据库中
        storeMapper.insert(entity);
        return StoreConvert.INSTANCE.entity2dto(entity);
    }

    /**
     * 新增员工
     *
     * @param staffDTO 员工信息
     * @return 新增成功的元信息
     * @throws BusinessException
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //参数合法校验
        if (staffDTO == null ||
                StringUtil.isBlank(staffDTO.getMobile()) ||
                StringUtil.isBlank(staffDTO.getUsername()) ||
                staffDTO.getStoreId() == null
        ) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //在同一个商户下员工的账号唯一
        boolean existStaffByUserName = isExistStaffByUserName(staffDTO.getUsername(), staffDTO.getMerchantId());
        if (existStaffByUserName) {
            throw new BusinessException(CommonErrorCode.E_100114);
        }
        //在同一个商户下员工的手机号唯一
        Boolean existStaffByMobile = isExistStaffByMobile(staffDTO.getMobile(), staffDTO.getMerchantId());
        if (existStaffByMobile) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        //将传入的对象进行转化
        Staff entity = StaffConvert.INSTANCE.dto2entity(staffDTO);
        //将数据插入到数据库中
        staffMapper.insert(entity);
        return StaffConvert.INSTANCE.entity2dto(entity);
    }

    /**
     * 将员工和门店进行绑定(设置管理员)
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        if (storeId == null || staffId == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStaffId(staffId);
        storeStaff.setStoreId(storeId);
        storeStaffMapper.insert(storeStaff);
    }

    /**
     * 根据商户id查询租户信息
     *
     * @param tenantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getTenantId, tenantId));
        //使用对象转化器进行转化
        MerchantDTO merchantDTO = MerchantCovert.INSTANCE.merchant2merchantDTO(merchant);
        return merchantDTO;
    }

    /**
     * 在同一个商户下员工的手机号唯一
     *
     * @param mobile
     * @param merchantId
     * @return
     */
    private Boolean isExistStaffByMobile(String mobile, Long merchantId) {
        Integer count = staffMapper.selectCount(
                new LambdaQueryWrapper<Staff>()
                        .eq(Staff::getMobile, mobile)
                        .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 根据账号判断员工是否已在指定商户存在
     *
     * @param userName
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByUserName(String userName, Long merchantId) {
        int count = staffMapper.selectCount(
                new LambdaQueryWrapper<Staff>()
                        .eq(Staff::getUsername, userName)
                        .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }
}
