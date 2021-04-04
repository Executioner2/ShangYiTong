package com.study.yygh.exception;

import com.study.yygh.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-17 19:12
 * Versions:1.0.0
 * Description:
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    /**
     * 自定义的全局异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = YyghException.class)
    @ResponseBody
    public Result error(YyghException e){
        return Result.build(e.getCode(), e.getMessage());
    }
}
