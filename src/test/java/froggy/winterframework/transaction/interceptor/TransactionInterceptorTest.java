package froggy.winterframework.transaction.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import froggy.winterframework.aop.MethodInvocation;
import froggy.winterframework.transaction.TransactionException;
import froggy.winterframework.transaction.TransactionManager;
import froggy.winterframework.transaction.TransactionStatus;
import froggy.winterframework.transaction.annotation.Transactional;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionInterceptorTest {

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    private TransactionInterceptor interceptor;

    @Before
    public void setUp() {
        interceptor = new TransactionInterceptor(transactionManager, new TransactionalMethodMatcher());
    }

    @Test
    public void 생성자에_null_TransactionManager를_전달하면_예외를_던진다() {
        // Given
        TransactionManager nullTransactionManager = null;

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> new TransactionInterceptor(nullTransactionManager, new TransactionalMethodMatcher())
        );

        // Then
        assertEquals("TransactionManager must not be null", actualException.getMessage());
    }

    @Test
    public void 생성자에_null_TransactionalMethodMatcher를_전달하면_예외를_던진다() {
        // Given
        TransactionalMethodMatcher nullMethodMatcher = null;

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> new TransactionInterceptor(transactionManager, nullMethodMatcher)
        );

        // Then
        assertEquals("TransactionalMethodMatcher must not be null", actualException.getMessage());
    }

    @Test
    public void Transactional이_없는_method는_transaction_없이_target을_실행한다() throws Throwable {
        // Given
        MethodInvocation invocation = returningInvocation(new NonTransactionalService(), "ok");

        // When
        Object result = interceptor.invoke(invocation);

        // Then
        assertEquals("ok", result);
        verify(invocation).proceed();
        verify(transactionManager, never()).begin();
        verify(transactionManager, never()).commit(any(TransactionStatus.class));
        verify(transactionManager, never()).rollback(any(TransactionStatus.class));
    }

    @Test
    public void 구현체_method가_Transactional이면_transaction을_commit한다() throws Throwable {
        // Given
        given(transactionManager.begin()).willReturn(transactionStatus);
        MethodInvocation invocation = returningInvocation(new MethodTransactionalService(), "ok");

        // When
        Object result = interceptor.invoke(invocation);

        // Then
        assertEquals("ok", result);
        verify(invocation).proceed();
        verify(transactionManager).begin();
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    public void interface_method가_Transactional이면_transaction을_commit한다() throws Throwable {
        // Given
        given(transactionManager.begin()).willReturn(transactionStatus);
        MethodInvocation invocation = returningInvocation(
            new TransactionalInterfaceImpl(),
            interfaceTransactionalExecuteMethod(),
            "ok"
        );

        // When
        Object result = interceptor.invoke(invocation);

        // Then
        assertEquals("ok", result);
        verify(invocation).proceed();
        verify(transactionManager).begin();
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    public void interface_method가_Transactional이고_target_예외가_발생하면_rollback한다() throws Throwable {
        // Given
        given(transactionManager.begin()).willReturn(transactionStatus);
        RuntimeException targetFailure = new RuntimeException("target failed");
        MethodInvocation invocation = throwingInvocation(
            new TransactionalInterfaceImpl(),
            interfaceTransactionalExecuteMethod(),
            targetFailure
        );

        // When
        RuntimeException actualException = assertThrows(
            RuntimeException.class,
            () -> interceptor.invoke(invocation)
        );

        // Then
        assertSame(targetFailure, actualException);
        verify(invocation).proceed();
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(transactionStatus);
    }

    @Test
    public void begin이_실패하면_target을_실행하지_않는다() throws Throwable {
        // Given
        TransactionException beginFailure = new TransactionException("begin failed");
        given(transactionManager.begin()).willThrow(beginFailure);
        MethodInvocation invocation = returningInvocation(new MethodTransactionalService(), "ok");

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> interceptor.invoke(invocation)
        );

        // Then
        assertSame(beginFailure, actualException);
        verify(invocation, never()).proceed();
        verify(transactionManager, never()).commit(any(TransactionStatus.class));
        verify(transactionManager, never()).rollback(any(TransactionStatus.class));
    }

    @Test
    public void target_예외가_발생하면_rollback하고_원래_예외를_다시_던진다() throws Throwable {
        // Given
        given(transactionManager.begin()).willReturn(transactionStatus);
        RuntimeException targetFailure = new RuntimeException("target failed");
        MethodInvocation invocation = throwingInvocation(new MethodTransactionalService(), targetFailure);

        // When
        RuntimeException actualException = assertThrows(
            RuntimeException.class,
            () -> interceptor.invoke(invocation)
        );

        // Then
        assertSame(targetFailure, actualException);
        verify(invocation).proceed();
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(transactionStatus);
    }

    @Test
    public void rollback이_실패하면_원래_예외에_suppressed로_보관한다() throws Throwable {
        // Given
        given(transactionManager.begin()).willReturn(transactionStatus);
        RuntimeException targetFailure = new RuntimeException("target failed");
        AssertionError rollbackFailure = new AssertionError("rollback failed");
        willThrow(rollbackFailure).given(transactionManager).rollback(transactionStatus);
        MethodInvocation invocation = throwingInvocation(new MethodTransactionalService(), targetFailure);

        // When
        RuntimeException actualException = assertThrows(
            RuntimeException.class,
            () -> interceptor.invoke(invocation)
        );

        // Then
        assertSame(targetFailure, actualException);
        assertEquals(1, actualException.getSuppressed().length);
        assertSame(rollbackFailure, actualException.getSuppressed()[0]);
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(transactionStatus);
    }

    @Test
    public void commit이_실패하면_rollback하지_않고_commit_예외를_던진다() throws Throwable {
        // Given
        given(transactionManager.begin()).willReturn(transactionStatus);
        TransactionException commitFailure = new TransactionException("commit failed");
        willThrow(commitFailure).given(transactionManager).commit(transactionStatus);
        MethodInvocation invocation = returningInvocation(new MethodTransactionalService(), "ok");

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> interceptor.invoke(invocation)
        );

        // Then
        assertSame(commitFailure, actualException);
        verify(invocation).proceed();
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    public interface TestService {

        String execute();
    }

    public static class NonTransactionalService implements TestService {

        @Override
        public String execute() {
            return "ok";
        }
    }

    public static class MethodTransactionalService implements TestService {

        @Override
        @Transactional
        public String execute() {
            return "ok";
        }
    }

    public interface TransactionalInterface {

        @Transactional
        String execute();
    }

    public static class TransactionalInterfaceImpl implements TransactionalInterface {

        @Override
        public String execute() {
            return "ok";
        }
    }

    private MethodInvocation returningInvocation(Object target, Object result) throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(target, executeMethod());
        given(invocation.proceed()).willReturn(result);
        return invocation;
    }

    private MethodInvocation returningInvocation(Object target, Method method, Object result) throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(target, method);
        given(invocation.proceed()).willReturn(result);
        return invocation;
    }

    private MethodInvocation throwingInvocation(Object target, Throwable failure) throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(target, executeMethod());
        given(invocation.proceed()).willThrow(failure);
        return invocation;
    }

    private MethodInvocation throwingInvocation(Object target, Method method, Throwable failure) throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(target, method);
        given(invocation.proceed()).willThrow(failure);
        return invocation;
    }

    private MethodInvocation mockMethodInvocation(Object target, Method method) {
        MethodInvocation invocation = mock(MethodInvocation.class);
        given(invocation.getTarget()).willReturn(target);
        given(invocation.getMethod()).willReturn(method);
        return invocation;
    }

    private Method executeMethod() throws NoSuchMethodException {
        return TestService.class.getMethod("execute");
    }

    private Method interfaceTransactionalExecuteMethod() throws NoSuchMethodException {
        return TransactionalInterface.class.getMethod("execute");
    }
}
