package com.shanjupay.merchant.service;

import org.springframework.web.multipart.MultipartFile;

import java.sql.BatchUpdateException;

public interface FileService {
    /**
     * 上传文件到OSS
     * @param file 文件
     * @return 文件访问路径
     * @throws BatchUpdateException
     */
    public String upload(MultipartFile file) throws BatchUpdateException;


}
