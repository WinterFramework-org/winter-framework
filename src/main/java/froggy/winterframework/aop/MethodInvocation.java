package froggy.winterframework.aop;

import java.lang.reflect.Method;

public interface MethodInvocation {

    Object getProxy();

    Object getTarget();

    Method getMethod();

    Object[] getArguments();

    Object proceed() throws Throwable;

}
