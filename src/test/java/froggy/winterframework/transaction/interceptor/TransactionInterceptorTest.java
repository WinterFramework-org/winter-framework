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
        interceptor = new TransactionInterceptor(transactionManager);
    }

    @Test
    public void 생성자에_null_TransactionManager를_전달하면_예외를_던진다() {
        // Given
        TransactionManager nullTransactionManager = null;

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> new TransactionInterceptor(nullTransactionManager)
        );

        // Then
        assertEquals("TransactionManager must not be null", actualException.getMessage());
    }

    @Test
    public void Transactional이_없는_method는_transaction_없이_target을_실행한다() throws Throwable {
        // Given
        MethodInvocation invocation = returningInvocation(new PlainService(), "ok");

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
        MethodInvocation invocation = returningInvocation(new TransactionalService(), "ok");

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
    public void begin이_실패하면_target을_실행하지_않는다() throws Throwable {
        // Given
        TransactionException beginFailure = new TransactionException("begin failed");
        given(transactionManager.begin()).willThrow(beginFailure);
        MethodInvocation invocation = returningInvocation(new TransactionalService(), "ok");

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
        MethodInvocation invocation = throwingInvocation(new TransactionalService(), targetFailure);

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
        MethodInvocation invocation = throwingInvocation(new TransactionalService(), targetFailure);

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
        MethodInvocation invocation = returningInvocation(new TransactionalService(), "ok");

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

    public interface PlainOperations {

        String execute();
    }

    public static class PlainService implements PlainOperations {

        @Override
        public String execute() {
            return "ok";
        }
    }

    public static class TransactionalService implements PlainOperations {

        @Override
        @Transactional
        public String execute() {
            return "ok";
        }
    }

    private MethodInvocation returningInvocation(Object target, Object result) throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(target);
        given(invocation.proceed()).willReturn(result);
        return invocation;
    }

    private MethodInvocation throwingInvocation(Object target, Throwable failure) throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(target);
        given(invocation.proceed()).willThrow(failure);
        return invocation;
    }

    private MethodInvocation mockMethodInvocation(Object target) throws NoSuchMethodException {
        MethodInvocation invocation = mock(MethodInvocation.class);
        given(invocation.getTarget()).willReturn(target);
        given(invocation.getMethod()).willReturn(executeMethod());
        return invocation;
    }

    private Method executeMethod() throws NoSuchMethodException {
        return PlainOperations.class.getMethod("execute");
    }
}
