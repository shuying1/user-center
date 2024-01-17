package com.yupi.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author yings
 * @create 2022-05-25 16:44
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 4772243295366861457L;
    private String userAccount;
    private String userPassword;


}
