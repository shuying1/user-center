package com.yupi.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author yings
 * @create 2022-05-25 16:44
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -2938132144433162638L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
