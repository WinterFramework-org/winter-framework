package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.aop.framework.ProxyFactory;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.config.BeanPostProcessor;
import froggy.winterframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;

/**
 * transaction 대상 bean을 {@link TransactionInterceptor}가 적용된 proxy로 대체하는
 * {@link BeanPostProcessor}.
 *
 * transaction 대상 method 여부는 {@link TransactionalMethodMatcher}가 판단한다.
 */
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
