package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.aop.MethodInterceptor;
import froggy.winterframework.aop.MethodInvocation;
import froggy.winterframework.stereotype.Component;

@Component
public class TransactionInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }
}
