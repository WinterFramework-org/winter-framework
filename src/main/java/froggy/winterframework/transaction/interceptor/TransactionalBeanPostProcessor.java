package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.aop.framework.ProxyFactory;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.config.BeanPostProcessor;
import froggy.winterframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;

@Component
public class TransactionalBeanPostProcessor implements BeanPostProcessor {

    private final ProxyFactory proxyFactory;
    private final TransactionInterceptor transactionInterceptor;
    private final TransactionalMethodMatcher methodMatcher;

    @Autowired
    public TransactionalBeanPostProcessor(
            ProxyFactory proxyFactory,
            TransactionInterceptor transactionInterceptor,
            TransactionalMethodMatcher methodMatcher
    ) {
        this.proxyFactory = proxyFactory;
        this.transactionInterceptor = transactionInterceptor;
        this.methodMatcher = methodMatcher;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!hasTransactionalMethod(bean.getClass())) {
            return bean;
        }

        return proxyFactory.createProxy(bean, Collections.singletonList(transactionInterceptor));
    }

    private boolean hasTransactionalMethod(Class<?> beanClass) {
        for (Method method : beanClass.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())
                && methodMatcher.matches(method, beanClass)) {
                return true;
            }
        }

        return false;
    }
}
