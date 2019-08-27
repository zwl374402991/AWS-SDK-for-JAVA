package com.awsapi.service;

import com.awsapi.module.Accounts;
import com.awsapi.module.CreateAccountStatus;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.organizations.OrganizationsClient;
import software.amazon.awssdk.services.organizations.model.Account;
import software.amazon.awssdk.services.organizations.model.CreateAccountRequest;
import software.amazon.awssdk.services.organizations.model.CreateAccountResponse;
import software.amazon.awssdk.services.organizations.model.ListAccountsRequest;
import software.amazon.awssdk.services.organizations.model.ListAccountsResponse;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AWSOrganizationsApi
 * 参考文档: https://docs.aws.amazon.com/organizations/latest/APIReference/API_CreateAccount.html
 * @author archerzhang
 */
@Service
public class AWSOrganizationsService {

    private static final Logger log = LoggerFactory.getLogger(AWSOrganizationsService.class);

    //aws - accessKeyId
    @Value("${reseller.aws.accessKeyId}")
    private String accessKeyId;

    //aws - secretAccessKey
    @Value("${reseller.aws.secretAccessKey}")
    private String secretAccessKey;

    //设置服务所在区域
    private Region osRegion = Region.US_EAST_1;

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
     * 需要使用AWS那种服务就初始化该服务的客户端
     * 账户服务客户端初始化
     */
    private OrganizationsClient osClientInit(){
        OrganizationsClient organizationsClient = OrganizationsClient.builder().region(osRegion).build();
        return organizationsClient;
    }

    /**
     * 创建awsOrganizations子账户
     * @param email 邮箱账户(邮箱不能重复 创建好后可以修改)
     * @param accountName AWS账户名称（名称可以重复）
     * @return ResponseVO
     */
    private JSONObject createAccountAWS(String email, String accountName) {
        log.info("createAccountAWS begin");
        OrganizationsClient organizationsClient = osClientInit();
        JSONObject jsonObject = new JSONObject();
        CreateAccountRequest request = CreateAccountRequest.builder().email(email).accountName(accountName).build();
        CreateAccountResponse response = organizationsClient.createAccount(request);
        log.info("createAccountAWS>>>>>>>>>>>>>>>>>>>>>>>>>>>CreateAccountResponse:"+response);
        CreateAccountStatus createAccountStatus = new CreateAccountStatus();
        createAccountStatus.setAccountId(response.createAccountStatus().accountId());
        createAccountStatus.setAccountName(response.createAccountStatus().accountName());
        createAccountStatus.setCompletedTimestamp(Date.from(response.createAccountStatus().completedTimestamp()));
        createAccountStatus.setFailureReason(response.createAccountStatus().failureReasonAsString());
        createAccountStatus.setGovCloudAccountId(response.createAccountStatus().govCloudAccountId());
        createAccountStatus.setId(response.createAccountStatus().id());
        createAccountStatus.setRequestedTimestamp(Date.from(response.createAccountStatus().requestedTimestamp()));
        createAccountStatus.setState(response.createAccountStatus().state().toString());
        log.info("createAccountAWS>>>>>>>>>>>>>>>>>>>>>>>>>>>awsAccount:"+createAccountStatus);
        jsonObject.put("code",200);
        jsonObject.put("msg","success");
        jsonObject.put("data",createAccountStatus);
        return jsonObject;
    }

    /**
     * 查询awsOrganizations账户信息列表
     * aws每次查询最大数量为20条，如账户数量超过20，则每次查询结果会附加nextToken
     * 重复请求此接口，并带上nextToken，则可以查询出所有数据，直到nextToken为null
     * 首次请求nextToken为Null
     * @return
     */
    public JSONObject listAccounts() {
        OrganizationsClient organizationsClient = osClientInit();
        List<Accounts> resultList = new ArrayList<Accounts>();
        resultList = queryNextAccount(organizationsClient,resultList,null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",200);
        jsonObject.put("msg","success");
        jsonObject.put("data",resultList);
        return jsonObject;
    }

    /**
     * 递归查询所有账户,直到nextToken为Null
     * @param organizationsClient AWS账户服务客户端
     * @param resultList 查询账户列表
     * @param nextToken AWS每次查询最多只有20条数据,如超过则回返回nextToken,下次查询作为请求参数
     * @return List<AWSAccount>
     * @throws SdkClientException AWS SDK for java
     */
    private static List<Accounts> queryNextAccount(OrganizationsClient organizationsClient,
                                                     List<Accounts> resultList,
                                                     String nextToken) throws SdkClientException {
        log.info("queryNextAccount begin");
        ListAccountsRequest request = ListAccountsRequest.builder().maxResults(20).nextToken(nextToken).build();
        ListAccountsResponse response = organizationsClient.listAccounts(request);
        List<Account> accounts = response.accounts();

        for (Account account : accounts) {
            Accounts awsAccount = new Accounts();
            awsAccount.setArn(account.arn());
            awsAccount.setEmail(account.email());
            awsAccount.setId(account.id());
            awsAccount.setJoinedMethod(account.joinedMethod().toString());
            awsAccount.setJoinedTimestamp(Date.from(account.joinedTimestamp()));
            awsAccount.setName(account.name());
            awsAccount.setStatus(account.status().toString());
            resultList.add(awsAccount);
        }
        nextToken = response.nextToken();
        if ( nextToken != null ) {
            queryNextAccount(organizationsClient, resultList, nextToken);
        }
        return resultList;
    }
}
