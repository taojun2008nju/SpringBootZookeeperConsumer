package com.test.config;

import com.test.interceptor.TraceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 * 
 * @author shenweirong@ysten.com
 * @date 2018年10月15日 下午2:25:57
 */
@Configuration
public class WebAppConfig implements WebMvcConfigurer {
    /**
     * 日志拦截
     */
    @Autowired
    private TraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns：需要拦截的访问路径
        // excludePathPatterns：不需要拦截的路径，String数组类型可以写多个用","分割
        registry.addInterceptor(traceInterceptor).addPathPatterns("/**");
        // .excludePathPatterns("/api/getToken","/success");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
