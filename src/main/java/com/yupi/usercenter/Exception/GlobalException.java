package com.yupi.usercenter.Exception;

import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.omg.SendingContext.RunTime;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yings
 * @create 2022-05-30 12:23
 */
@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessException(BusinessException e){
        log.error("BusinessException:"+e.getMessage(),e);
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse businessException(RuntimeException e){
        log.error("RuntimeException:"+e.getMessage(),e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(),e.getMessage(),"");
    }
}
