package com.awsapi.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * aws查询账户状态返回结果
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Accounts {

    private String arn;
    private String email;

    //accountId AWS账户唯一标识
    private String id;
    private String joinedMethod;
    private Date joinedTimestamp;
    private String name;

    //账户状态区别于创建状态
    private String status;


}
