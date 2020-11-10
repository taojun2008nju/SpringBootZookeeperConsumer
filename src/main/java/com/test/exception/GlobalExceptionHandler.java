package com.test.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @date 2020/6/21 21:04:00
 * @description TODO
 */
@ControllerAdvice    //"控制器增强"注解
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)        //用于注释异常处理类，value属性指定需要拦截的异常类型
    @ResponseBody                            //和controller方法上的用法一样，会将方法中的返回值转json后返回给客户端
    public Map<String, Object> errorHandler(Exception e) {        //捕获异常并获取异常处理对象
        log.error("Method:errorHandler e:{}", e);

        Map<String, Object> result = new HashMap<String, Object>();

        result.put("code", "0");
        result.put("msg", e.getMessage());    //获取异常信息

        return result;        //将异常信息响应给浏览器
    }
}
