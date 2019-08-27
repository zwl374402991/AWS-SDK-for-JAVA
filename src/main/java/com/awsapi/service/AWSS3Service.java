package com.awsapi.service;

import com.alibaba.fastjson.JSONObject;
import com.awsapi.utils.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

/**
 * AWSS3Api
 * 参考文档: https://docs.aws.amazon.com/zh_cn/AmazonS3/latest/API/RESTObjectGET.html
 * 在aws管理控制台创建一个S3桶，并创建账单报告后，可通过接口下载账单报告文件
 * 同样S3桶也可以通过接口创建，此处不做示例
 * @author archerzhang
 */
@Service
public class AWSS3Service {

    private static final Logger log = LoggerFactory.getLogger(AWSOrganizationsService.class);

    //aws - accessKeyId
    @Value("${reseller.aws.accessKeyId}")
    private String accessKeyId;

    //aws - secretAccessKey
    @Value("${reseller.aws.secretAccessKey}")
    private String secretAccessKey;

    //aws - s3桶名
    @Value("${reseller.aws.bucketName}")
    private String bucketName;

    //aws - s3桶中文件下载key
    @Value("${reseller.aws.billKey}")
    private String billKey;

    //文件下载存储路径
    @Value("${reseller.tempRootDir}")
    private String tempRootDir;


    //设置S3桶服务所在区域
    private Region s3Region = Region.AP_NORTHEAST_1;

    /**
     * 要使用AWS API 需要先提供aws安全凭证
     * aws官方提供了4中加载方式，这里采用java系统属性的方式进行加载
     * 初始化aws安全凭证
     */
    @PostConstruct
    public void setKeyInit(){
        System.setProperty("aws.accessKeyId", this.accessKeyId);
        System.setProperty("aws.secretAccessKey", this.secretAccessKey);
    }

    /**
     * 初始化根路径
     */
    @PostConstruct
    public void init() {
        File tempDirFile = new File(tempRootDir);
        if (!tempDirFile.exists()) {
            tempDirFile.mkdirs();
        }
    }

    /**
     * s3桶服务客户端初始化
     */
    private S3Client s3ClientInit(){
        S3Client s3Client = S3Client.builder().region(s3Region).build();
        return s3Client;
    }


    /**
     * S3桶账单报告下载
     * @return
     */
    public JSONObject amazonS3Downloading() {
        log.info("amazonS3Downloading begin");
        String filePath = tempRootDir + "/" + this.billKey.split("/")[3];
        S3Client s3Client = s3ClientInit();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(this.bucketName).key(this.billKey).build();
        GetObjectResponse getObjectResponse =
                s3Client.getObject(getObjectRequest, ResponseTransformer.toFile(Paths.get(tempRootDir, this.billKey.split("/")[3])));
        log.info("amazonS3Downloading>>>>>>>>>>>>>>>>>>>>>>>>>>>GetObjectResponse:" + getObjectResponse);
        File zipFile = new File(tempRootDir + this.billKey.split("/")[3]);
        ZipUtil.unZip(zipFile, tempRootDir);
        Boolean isDelete = zipFile.delete();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 200);
        jsonObject.put("msg", "success");
        return jsonObject;
    }
}
