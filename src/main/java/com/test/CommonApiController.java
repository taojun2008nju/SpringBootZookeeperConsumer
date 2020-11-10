package com.test;

import com.alibaba.dubbo.config.annotation.Reference;
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
    public String testApi() throws Exception {
        log.info("Method:testApi");
        testService.test("123");
        return "Hello World";
    }
}