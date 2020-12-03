package com.test.hystrix;

import com.alibaba.dubbo.common.extension.SPI;

/**
 * @author Administrator
 * @date 2020/12/3 20:25:00
 * @description 降级方法接口
 */
//@SPI
public interface Fallback {

    Object invoker();
}
