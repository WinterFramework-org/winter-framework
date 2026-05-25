package froggy.winterframework.aop;

@FunctionalInterface
public interface MethodInterceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;

}
