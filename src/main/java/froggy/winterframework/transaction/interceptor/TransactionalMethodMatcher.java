package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.stereotype.Component;
import froggy.winterframework.transaction.annotation.Transactional;
import java.lang.reflect.Method;

/**
 * {@link Transactional} annotation 기준으로 transaction 대상 method를 판별하는 matcher.
 */
@Component
public class TransactionalMethodMatcher {

    /**
     * 전달된 method 자체 또는 target class와 상위 class/interface의 같은 signature method에
     * {@link Transactional} annotation이 있는지 반환한다.
     *
     * @param method proxy invocation에서 전달된 method
     * @param targetClass 실제 target object의 class
     * @return {@link Transactional} annotation이 발견되면 true, 아니면 false
     */
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
