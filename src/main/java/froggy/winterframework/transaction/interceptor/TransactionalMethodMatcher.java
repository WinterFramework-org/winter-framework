package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.stereotype.Component;
import froggy.winterframework.transaction.annotation.Transactional;
import java.lang.reflect.Method;

@Component
public class TransactionalMethodMatcher {

    public boolean matches(Method method, Class<?> targetClass) {
        if (method.isAnnotationPresent(Transactional.class)) {
            return true;
        }

        if (hasTransactionalMethod(targetClass, method)) {
            return true;
        }

        return false;
    }

    private boolean hasTransactionalMethod(Class<?> targetClass, Method method) {
        Method targetMethod = getMethodIfPresent(targetClass, method);
        if (targetMethod != null && targetMethod.isAnnotationPresent(Transactional.class)) {
            return true;
        }

        for (Class<?> interfaceClass : targetClass.getInterfaces()) {
            if (hasTransactionalMethod(interfaceClass, method)) {
                return true;
            }
        }

        Class<?> superClass = targetClass.getSuperclass();
        return superClass != null && hasTransactionalMethod(superClass, method);
    }

    private Method getMethodIfPresent(Class<?> targetClass, Method method) {
        try {
            return targetClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
