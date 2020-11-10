package com.test.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

@Activate(group = {Constants.PROVIDER}, order = 1)
public class ProviderRpcTraceFilter implements Filter {

    /**
     * 日志追踪id
     */
    String LOG_TRACE_ID = "traceId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId = RpcContext.getContext().getAttachment(LOG_TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        // 设置日志traceId变量
        MDC.put(LOG_TRACE_ID, traceId);
        RpcContext.getContext().setAttachment(LOG_TRACE_ID, traceId);

        try {
            return invoker.invoke(invocation);
        } finally {
            MDC.remove(LOG_TRACE_ID);
        }
    }
}
