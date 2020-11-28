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
        fallbackMethod = "testApiFallback",
        ignoreExceptions = {NullPointerException.class, ArithmeticException.class},
        commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000" )
        })
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