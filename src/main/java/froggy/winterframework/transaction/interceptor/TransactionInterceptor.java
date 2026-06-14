package froggy.winterframework.transaction.interceptor;

import froggy.winterframework.aop.MethodInterceptor;
import froggy.winterframework.aop.MethodInvocation;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.transaction.TransactionManager;
import froggy.winterframework.transaction.TransactionStatus;
import froggy.winterframework.transaction.annotation.Transactional;
import java.lang.reflect.Method;

@Component
public class TransactionInterceptor implements MethodInterceptor {

    private static final String NULL_TRANSACTION_MANAGER_MESSAGE = "TransactionManager must not be null";

    private final TransactionManager transactionManager;

    @Autowired
    public TransactionInterceptor(TransactionManager transactionManager) {
        if (transactionManager == null) {
            throw new IllegalArgumentException(NULL_TRANSACTION_MANAGER_MESSAGE);
        }

        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!isTransactional(invocation)) {
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

    private boolean isTransactional(MethodInvocation invocation) throws NoSuchMethodException {
        Method targetMethod = getTargetMethod(invocation);
        return targetMethod.isAnnotationPresent(Transactional.class);
    }

    private Method getTargetMethod(MethodInvocation invocation) throws NoSuchMethodException {
        Method method = invocation.getMethod();
        return invocation.getTarget()
            .getClass()
            .getMethod(method.getName(), method.getParameterTypes());
    }
}
