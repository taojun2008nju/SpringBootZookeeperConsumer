package com.test.interceptor;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 日志追踪id生成
 * 
 * @author shenweirong@ysten.com
 * @date 2018年10月15日 下午1:57:10
 */
@Component
public class TraceInterceptor extends HandlerInterceptorAdapter {

    /**
     * 日志追踪id
     */
    String LOG_TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // "traceId"
        MDC.put(LOG_TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        return true;
    }
}
