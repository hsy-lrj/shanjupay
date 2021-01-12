package com.shanjupay.merchant.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.shanjupay.merchant.service.FileService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    /**
     * 分别从Nacos中获取值
     */
    @Value("${oss.aliyun.FileEndpoint}")
    private String endpoint;
    @Value("${oss.aliyun.AccessKeyId}")
    private String accessKeyId;
    @Value("${oss.aliyun.AccessKeySecret}")
    private String accessKeySecret;
    @Value("${oss.aliyun.FileBucketName}")
    private String bucketName;

    /**
     * 上传文件到OSS
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file) {

        try {
            //创建OSSClient实例。
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            //获取上传文件输入流
            InputStream inputStream = file.getInputStream();
            //获取文件名称
            String Filename = file.getOriginalFilename();
            //在文件名称里面添加一个唯一的标识
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            Filename=uuid+Filename;
            //给文件按照日期进行分类
            String time = new DateTime().toString("yyyy/MM/dd");
            Filename=time+"/"+Filename;
            /**
             * 调用oss方法进行上传
             * 参数：
             *      bucket名称
             *      上传到oss文件路径和文件名称
             *      上传文件的输入流
             */
            ossClient.putObject(bucketName, Filename, inputStream);
            // 关闭OSSClient。
            ossClient.shutdown();
            //把上传到阿里云oss的路径手动拼接并且返回
            return "https://" + bucketName + "." + endpoint + "/" + Filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
