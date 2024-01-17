package com.yupi.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yings
 * @create 2022-05-30 9:43
 */

/**
 * 通用返回类
 *
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private T data;
    private String message;
    private String description;

    public BaseResponse(int code, T data, String message,String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description=description;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "","");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(),errorCode.getDescription());

    }
    public BaseResponse(ErrorCode errorCode,String description) {
        this(errorCode.getCode(), null, errorCode.getMessage(),errorCode.getDescription());

    }


}
