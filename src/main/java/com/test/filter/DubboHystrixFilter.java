package com.test.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.test.hystrix.DubboHystrixCommand;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Administrator
 * @date 2020/12/3 20:14:00
 * @description Hystrix集成Filter
 */
@Activate(group = Constants.CONSUMER, order = 2)
public class DubboHystrixFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String isHystrixOpen = invoker.getUrl().getMethodParameter(invocation.getMethodName(), "isHystrixOpen");
        // 是否打开hystrix
        if (StringUtils.isNotEmpty(isHystrixOpen) && Boolean.parseBoolean(isHystrixOpen)) {
            DubboHystrixCommand dubboHystrixCommand = new DubboHystrixCommand(invoker, invocation);
            Result result = dubboHystrixCommand.execute();
            return result;
        } else {
            // 未打开的话, 直接走真正调用逻辑
            return invoker.invoke(invocation);
        }

    }
}
