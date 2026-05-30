package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.aop.framework.ProxyFactory;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.config.BeanPostProcessor;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.transaction.annotation.Transactional;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;

@Component
public class TransactionalBeanPostProcessor implements BeanPostProcessor {

    private final ProxyFactory proxyFactory;
    private final TransactionInterceptor transactionInterceptor;

    @Autowired
    public TransactionalBeanPostProcessor(
            ProxyFactory proxyFactory,
            TransactionInterceptor transactionInterceptor
    ) {
        this.proxyFactory = proxyFactory;
        this.transactionInterceptor = transactionInterceptor;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!hasTransactionalMethod(bean.getClass())) {
            return bean;
        }

        return proxyFactory.createProxy(bean, Collections.singletonList(transactionInterceptor));
    }

    private boolean hasTransactionalMethod(Class<?> beanClass) {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())
                && method.isAnnotationPresent(Transactional.class)) {
                return true;
            }
        }

        return false;
    }
}
