package com.test.service;

import com.test.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @date 2020/12/3 20:26:00
 * @description 降级方法实现类
 */
@Slf4j
@Service
public class TestServiceFallbackImpl implements Fallback {

    @Override
    public Object invoker() {
        log.info("hystrix fallBack.........invoke");
        return "hystrix fallBack";
    }
}
