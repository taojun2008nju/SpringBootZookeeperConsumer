package com.test;

import com.alibaba.dubbo.config.annotation.Reference;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.test.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CommonApiController {

    @Reference
    private TestService testService;

    @RequestMapping(value = "/testApi")
    @ResponseBody
    @HystrixCommand(
        // commandProperties熔断的⼀些细节属性配置
        commandProperties = {
            // 每⼀个属性都是⼀个HystrixProperty，HystrixCommandProperties可以获取配置信息
            //服务降级，设置超时时间2秒
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000"),
            //以下配置是熔断跳闸与自我修复：8秒钟内，请求次数达到2个，并且失败率在50%以上，就跳闸,跳闸后活动窗⼝设置为3s,即三秒后进行重试
            //统计时间窗口定义
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds",value = "8000"),
            //最小请求数量
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "2"),
            //统计时间框口内的异常百分比
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
            //自我修复活动窗口时长
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "3000")
        },
        fallbackMethod = "testApiFallback",
        ignoreExceptions = {ArithmeticException.class})
    public String testApi() throws Exception {
        log.info("Method:testApi");
        String testResult = testService.test("123");
        return testResult;
    }

    /**
     * 降级fallback方法
     * @return
     * @throws Exception
     */
    public String testApiFallback(Throwable throwable) throws Exception {
        log.error("Method:testApiFallback exception:", throwable);
        return "testApiFail FailBack";
    }
}