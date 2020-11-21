package com.test.aspect;

import com.test.constant.Constants;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @date 2020/11/21 15:04:00
 * @description MDC日志切面
 */
@Aspect
@Component
public class MDCAspect {

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Async)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MDC.put(Constants.LOG_TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        Object result = point.proceed();// 执行方法
        MDC.remove(Constants.LOG_TRACE_ID);
        return result;
    }
}
