package com.test.hystrix;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.test.util.SpringBeanUtil;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;

/**
 * @author Administrator
 * @date 2020/12/3 20:16:00
 * @description DubboHystrixCommand
 */
public class DubboHystrixCommand extends HystrixCommand<Result> {

    private Invoker<?> invoker ;
    private Invocation invocation;
    private String fallbackClass;
    static HashMap<String,Setter> setterHashMap = new HashMap<>();

    public DubboHystrixCommand(Invoker<?> invoker, Invocation invocation) {
        // 构造HystrixCommand.Setter
        super(HystrixCommand_Setter(invoker, invocation));
        this.invoker = invoker;
        this.invocation = invocation;
        //根据参数来获取降级方法名
        this.fallbackClass = invoker.getUrl().getMethodParameter(invocation.getMethodName(), "fallbackClass");
        ;
    }

    @Override
    protected Result run() throws Exception {
        Result result = invoker.invoke(invocation);
        // 如果远程调用异常，抛出异常就会调用getFallback()方法去执行降级逻辑
        if (result.hasException()) {
            throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.COMMAND_EXCEPTION,
                DubboHystrixCommand.class, result.getException().getMessage(),
                result.getException(), null);
        }

        return result;

    }

    @Override
    protected Result getFallback() {
        // 如果没有fallback, 则抛出原本的异常:No fallback available.
        if (StringUtils.isEmpty(fallbackClass)) {
            return super.getFallback();
        }
        try {
            // 基于SPI扩展加载fallback实现
//            ExtensionLoader<Fallback> loader = ExtensionLoader.getExtensionLoader(Fallback.class);
//            Fallback fallback = loader.getExtension(fallbackName);
            try {
                Class fallbackClazz = Class.forName(fallbackClass);
                Fallback fallback = (Fallback) SpringBeanUtil.getBean(fallbackClazz);
                Object value = fallback.invoker();
                return new RpcResult(value);
            } catch (ClassNotFoundException e) {
                return new RpcResult(e);
            }
        } catch (RuntimeException ex) {
            throw ex;
        }
    }


    private static HystrixCommand.Setter HystrixCommand_Setter(Invoker<?> invoker, Invocation invocation) {
        // interfaceName.methodName
        String key = String.format("%s.%s", invoker.getInterface().getName(), invocation.getMethodName());
        // 1 根据interfaceName+methodName从缓存获取Setter
        if (setterHashMap.containsKey(key)) {
            return setterHashMap.get(key);
        } else {
            setterHashMap.put(key,
                HystrixCommand.Setter
                    // 组名使用服务接口模块名称
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(invoker.getInterface().getName()))
                    // 隔离粒度为接口方法, 但是同一个接口中的所有方法公用一个线程池, 各个服务接口的线程池是隔离的
                    // 配置到这里, 就说明, 相同的接口服务, 相同的方法, 拥有相同的熔断配置策略
                    .andCommandKey(HystrixCommandKey.Factory.asKey(invocation.getMethodName()))
                    // 熔断配置
                    .andCommandPropertiesDefaults(hystrixCommandProperties_Setter(invoker.getUrl(),invocation.getMethodName()))
                    // 线程池配置
                    .andThreadPoolPropertiesDefaults(hystrixThreadPoolProperties_Setter(invoker.getUrl())));
            return setterHashMap.get(key);
        }
    }

    public static HystrixCommandProperties.Setter hystrixCommandProperties_Setter(URL url, String method) {
        // 从URL获取熔断配置
        return HystrixCommandProperties.Setter()
            // 熔断触发后多久恢复half-open状态,
            // 熔断后sleepWindowInMilliseconds毫秒会放入一个请求，如果请求处理成功，熔断器关闭，否则熔断器打开，继续等待sleepWindowInMilliseconds
            .withCircuitBreakerSleepWindowInMilliseconds(url.getMethodParameter(method,
                "sleepWindowInMilliseconds",
                3000))
            // 熔断触发错误率阈值, 超过50%错误触发熔断
            .withCircuitBreakerErrorThresholdPercentage(url.getMethodParameter(method,
                "errorThresholdPercentage",
                50))
            // 熔断判断请求数阈值, 一个统计周期内（默认10秒）请求不少于requestVolumeThreshold才会进行熔断判断
            .withCircuitBreakerRequestVolumeThreshold(url.getMethodParameter(method,
                "requestVolumeThreshold",
                2))
            // 这里可以禁用超时, 而采用dubbo的超时时间, 默认为true
            // .withExecutionTimeoutEnabled(false)
            // 当隔离策略为THREAD时，当执行线程执行超时时，是否进行中断处理，默认为true。
            .withExecutionIsolationThreadInterruptOnTimeout(true)
            // 执行超时时间，默认为1000毫秒，如果命令是线程隔离，且配置了executionIsolationThreadInterruptOnTimeout=true，则执行线程将执行中断处理。
            // 如果命令是信号量隔离，则进行终止操作，因为信号量隔离与主线程是在一个线程中执行，其不会中断线程处理，所以要根据实际情况来决定是否采用信号量隔离，尤其涉及网络访问的情况。
            // 注意该时间和dubbo自己的超时时间不要冲突，以这个时间优先，比如consumer设置3秒，那么当执行时hystrix会提前超时, 因为这里设置的时间为1秒
            .withExecutionTimeoutInMilliseconds(url.getMethodParameter(method,
                "timeoutInMilliseconds",
                1000))
            // fallback方法的信号量配置，配置getFallback方法并发请求的信号量，如果请求超过了并发信号量限制，则不再尝试调用getFallback方法，而是快速失败，默认信号量为10
            .withFallbackIsolationSemaphoreMaxConcurrentRequests(url.getMethodParameter(method,
                "fallbackMaxConcurrentRequests",
                50))
            // 隔离策略, 默认thread线程池隔离
            .withExecutionIsolationStrategy(getIsolationStrategy(url))
            // 设置隔离策略为ExecutionIsolationStrategy.SEMAPHORE时，HystrixCommand.run()方法允许的最大请求数。如果达到最大并发数时，后续请求会被拒绝。
            .withExecutionIsolationSemaphoreMaxConcurrentRequests(url.getMethodParameter(method,
                "maxConcurrentRequests",
                10));

    }

    private static String THREAD = "1";
    private static String SEMAPHORE = "2";

    public static HystrixCommandProperties.ExecutionIsolationStrategy getIsolationStrategy(URL url) {
        String isolation = url.getParameter("isolation", THREAD);
        if (!isolation.equalsIgnoreCase(THREAD) && !isolation.equalsIgnoreCase(SEMAPHORE)) {
            isolation = THREAD;
        }
        if (isolation.equalsIgnoreCase(THREAD)) {
            return HystrixCommandProperties.ExecutionIsolationStrategy.THREAD;
        } else {
            return HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE;
        }
    }

    public static HystrixThreadPoolProperties.Setter hystrixThreadPoolProperties_Setter(URL url) {
        // 从url获取线程池配置
        return HystrixThreadPoolProperties
            .Setter()
            .withCoreSize(url.getParameter("coreSize", 10))
            .withAllowMaximumSizeToDivergeFromCoreSize(true)
            .withMaximumSize(url.getParameter("maximumSize", 20))
            .withMaxQueueSize(-1)
            .withKeepAliveTimeMinutes(url.getParameter("keepAliveTimeMinutes", 1));
    }
}
