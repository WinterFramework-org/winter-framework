package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.aop.MethodInterceptor;
import froggy.winterframework.aop.MethodInvocation;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.transaction.TransactionManager;
import froggy.winterframework.transaction.TransactionStatus;

@Component
public class TransactionInterceptor implements MethodInterceptor {

    private static final String NULL_TRANSACTION_MANAGER_MESSAGE = "TransactionManager must not be null";
    private static final String NULL_METHOD_MATCHER_MESSAGE = "TransactionalMethodMatcher must not be null";

    private final TransactionManager transactionManager;
    private final TransactionalMethodMatcher methodMatcher;

    @Autowired
    public TransactionInterceptor(
            TransactionManager transactionManager,
            TransactionalMethodMatcher methodMatcher
    ) {
        if (transactionManager == null) {
            throw new IllegalArgumentException(NULL_TRANSACTION_MANAGER_MESSAGE);
        }
        if (methodMatcher == null) {
            throw new IllegalArgumentException(NULL_METHOD_MATCHER_MESSAGE);
        }

        this.transactionManager = transactionManager;
        this.methodMatcher = methodMatcher;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!methodMatcher.matches(invocation.getMethod(), invocation.getTarget().getClass())) {
            return invocation.proceed();
        }

        TransactionStatus status = transactionManager.begin();
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable targetFailure) {
            try {
                transactionManager.rollback(status);
            } catch (Throwable rollbackFailure) {
                targetFailure.addSuppressed(rollbackFailure);
            }
            throw targetFailure;
        }

        transactionManager.commit(status);
        return result;
    }
}
