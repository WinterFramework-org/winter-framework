package froggy.winterframework.aop.framework;

import froggy.winterframework.aop.MethodInterceptor;
import froggy.winterframework.aop.MethodInvocation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectiveMethodInvocation implements MethodInvocation {

    private final Object proxy;
    private final Object target;
    private final Method method;
    private final Object[] arguments;
    private final List<MethodInterceptor> interceptors;
    private int currentInterceptorIndex = -1;

    public ReflectiveMethodInvocation(
            Object proxy,
            Object target,
            Method method,
            Object[] arguments,
            List<MethodInterceptor> interceptors
    ) {
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target must not be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null");
        }
        if (interceptors == null) {
            throw new IllegalArgumentException("Interceptors must not be null");
        }
        for (MethodInterceptor interceptor : interceptors) {
            if (interceptor == null) {
                throw new IllegalArgumentException("Interceptor must not be null");
            }
        }

        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.arguments = arguments != null ? arguments : new Object[0];
        this.interceptors = Collections.unmodifiableList(new ArrayList<>(interceptors));
    }

    @Override
    public Object getProxy() {
        return proxy;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() throws Throwable {
        if (currentInterceptorIndex == interceptors.size() - 1) {
            return invokeTargetMethod();
        }

        MethodInterceptor interceptor = interceptors.get(++currentInterceptorIndex);
        return interceptor.invoke(this);
    }

    private Object invokeTargetMethod() throws Throwable {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

}
