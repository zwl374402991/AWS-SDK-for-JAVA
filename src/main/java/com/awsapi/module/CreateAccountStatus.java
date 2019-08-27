package com.awsapi.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * aws创建账户及查询创建账户的状态返回结果
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateAccountStatus {

    private String accountId;
    private String accountName;
    private Date completedTimestamp;
    private String failureReason;
    private String govCloudAccountId;
    private String id;
    private Date requestedTimestamp;
    private String state;


}
