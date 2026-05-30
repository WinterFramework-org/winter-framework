package froggy.winterframework.aop.framework;

import froggy.winterframework.aop.MethodInterceptor;
import froggy.winterframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class JdkProxyFactory implements ProxyFactory {

    @Override
    public Object createProxy(Object target, List<MethodInterceptor> interceptors) {
        if (target == null) {
            throw new IllegalArgumentException("Target must not be null");
        }
        if (interceptors == null) {
            throw new IllegalArgumentException("Interceptors must not be null");
        }
        for (MethodInterceptor interceptor : interceptors) {
            if (interceptor == null) {
                throw new IllegalArgumentException("Interceptor must not be null");
            }
        }

        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = findAllInterfaces(targetClass);
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("JDK proxy target must implement at least one interface: "
                + targetClass.getName());
        }

        List<MethodInterceptor> copiedInterceptors = Collections.unmodifiableList(new ArrayList<>(interceptors));
        return Proxy.newProxyInstance(
            targetClass.getClassLoader(),
            interfaces,
            (proxy, method, arguments) -> invoke(proxy, target, method, arguments, copiedInterceptors)
        );
    }

    private Class<?>[] findAllInterfaces(Class<?> targetClass) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = targetClass;
        while (current != null) {
            Collections.addAll(interfaces, current.getInterfaces());
            current = current.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[0]);
    }

    private Object invoke(
            Object proxy,
            Object target,
            Method method,
            Object[] arguments,
            List<MethodInterceptor> interceptors
    ) throws Throwable {
        if (isEqualsMethod(method)) {
            return arguments != null && arguments.length == 1 && proxy == arguments[0];
        }
        if (isHashCodeMethod(method)) {
            return System.identityHashCode(proxy);
        }
        if (isToStringMethod(method)) {
            return "JDK proxy for " + target.getClass().getName();
        }

        return new ReflectiveMethodInvocation(proxy, target, method, arguments, interceptors).proceed();
    }

    private boolean isEqualsMethod(Method method) {
        return method.getName().equals("equals")
            && method.getReturnType() == boolean.class
            && method.getParameterTypes().length == 1
            && method.getParameterTypes()[0] == Object.class;
    }

    private boolean isHashCodeMethod(Method method) {
        return method.getName().equals("hashCode")
            && method.getReturnType() == int.class
            && method.getParameterTypes().length == 0;
    }

    private boolean isToStringMethod(Method method) {
        return method.getName().equals("toString")
            && method.getReturnType() == String.class
            && method.getParameterTypes().length == 0;
    }

}
