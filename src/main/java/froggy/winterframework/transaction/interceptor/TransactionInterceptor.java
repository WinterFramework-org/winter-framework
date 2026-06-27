package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.aop.MethodInterceptor;
import froggy.winterframework.aop.MethodInvocation;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.transaction.TransactionManager;
import froggy.winterframework.transaction.TransactionStatus;

/**
 * transaction 대상 method 호출을 transaction 경계 안에서 실행하는 AOP interceptor.
 *
 * transaction 대상 여부는 {@link TransactionalMethodMatcher}가 판단하고,
 * transaction의 구체적인 처리는 {@link TransactionManager} 구현체가 수행한다.
 */
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

    /**
     * transaction 대상인 호출은 transaction 경계 안에서 target method를 실행하고,
     * 대상이 아닌 호출은 transaction 처리 없이 그대로 실행한다.
     *
     * target method 호출이 예외 없이 끝나면 commit한다. {@link Throwable}을 던지면
     * rollback을 시도한 뒤 target method가 던진 Throwable을 다시 던진다.
     * rollback 중 발생한 Throwable은 원래 Throwable에 suppressed로 추가한다.
     * commit 중 발생한 Throwable은 그대로 전파하며, 이 경우 rollback은 시도하지 않는다.
     */
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
