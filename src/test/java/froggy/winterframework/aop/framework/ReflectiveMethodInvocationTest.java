package froggy.winterframework.aop.framework;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import froggy.winterframework.aop.MethodInterceptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ReflectiveMethodInvocationTest {

    @Test
    public void 인터셉터가_없으면_대상_메서드를_바로_실행한다() throws Throwable {
        // Given
        CountingTarget target = new CountingTarget();
        Method method = CountingTarget.class.getMethod("add", int.class, int.class);
        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[]{1, 2},
            Collections.emptyList()
        );

        // When
        Object result = invocation.proceed();

        // Then
        assertEquals(3, result);
        assertEquals(1, target.callCount);
    }

    @Test
    public void 인터셉터를_등록_순서대로_실행한다() throws Throwable {
        // Given
        List<String> executionOrder = new ArrayList<>();
        EventRecordingTarget target = new EventRecordingTarget(executionOrder);
        Method method = EventRecordingTarget.class.getMethod("execute");

        MethodInterceptor outer = invocation -> {
            executionOrder.add("outer-before");
            Object result = invocation.proceed();
            executionOrder.add("outer-after");
            return result;
        };
        MethodInterceptor inner = invocation -> {
            executionOrder.add("inner-before");
            Object result = invocation.proceed();
            executionOrder.add("inner-after");
            return result;
        };

        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[0],
            Arrays.asList(outer, inner)
        );

        // When
        Object result = invocation.proceed();

        // Then
        assertEquals("ok", result);
        assertEquals(Arrays.asList(
            "outer-before",
            "inner-before",
            "target",
            "inner-after",
            "outer-after"
        ), executionOrder);
    }

    @Test
    public void 인터셉터가_proceed를_호출하지_않으면_대상_메서드를_실행하지_않는다() throws Throwable {
        // Given
        CountingTarget target = new CountingTarget();
        Method method = CountingTarget.class.getMethod("add", int.class, int.class);
        MethodInterceptor interceptor = invocation -> "skipped";

        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[]{1, 2},
            Collections.singletonList(interceptor)
        );

        // When
        Object result = invocation.proceed();

        // Then
        assertEquals("skipped", result);
        assertEquals(0, target.callCount);
    }

    @Test
    public void 인터셉터에서_현재_호출_정보를_조회할_수_있다() throws Throwable {
        // Given
        Object proxy = new Object();
        CountingTarget target = new CountingTarget();
        Method method = CountingTarget.class.getMethod("add", int.class, int.class);
        Object[] arguments = {1, 2};

        MethodInterceptor interceptor = invocation -> {
            assertSame(proxy, invocation.getProxy());
            assertSame(target, invocation.getTarget());
            assertSame(method, invocation.getMethod());
            assertArrayEquals(arguments, invocation.getArguments());
            return invocation.proceed();
        };

        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            proxy,
            target,
            method,
            arguments,
            Collections.singletonList(interceptor)
        );

        // When
        Object result = invocation.proceed();

        // Then
        assertEquals(3, result);
    }

    @Test
    public void 각_invocation은_인터셉터_체인을_독립적으로_실행한다() throws Throwable {
        // Given
        CountingTarget target = new CountingTarget();
        Method method = CountingTarget.class.getMethod("add", int.class, int.class);
        AtomicInteger interceptorCallCount = new AtomicInteger();
        MethodInterceptor interceptor = invocation -> {
            interceptorCallCount.incrementAndGet();
            return invocation.proceed();
        };
        List<MethodInterceptor> interceptors = Collections.singletonList(interceptor);

        // When
        Object firstResult = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[]{1, 2},
            interceptors
        ).proceed();
        Object secondResult = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[]{3, 4},
            interceptors
        ).proceed();

        // Then
        assertEquals(3, firstResult);
        assertEquals(7, secondResult);
        assertEquals(2, interceptorCallCount.get());
        assertEquals(2, target.callCount);
    }

    @Test
    public void 인자가_null이면_빈_배열로_처리한다() throws Throwable {
        // Given
        NoArgumentTarget target = new NoArgumentTarget();
        Method method = NoArgumentTarget.class.getMethod("execute");

        MethodInterceptor interceptor = invocation -> {
            assertArrayEquals(new Object[0], invocation.getArguments());
            return invocation.proceed();
        };

        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            null,
            Collections.singletonList(interceptor)
        );

        // When
        Object result = invocation.proceed();

        // Then
        assertEquals("ok", result);
    }

    @Test
    public void 생성_후_원본_인터셉터_목록이_바뀌어도_체인은_변하지_않는다() throws Throwable {
        // Given
        CountingTarget target = new CountingTarget();
        Method method = CountingTarget.class.getMethod("add", int.class, int.class);
        List<MethodInterceptor> interceptors = new ArrayList<>();
        MethodInterceptor passThroughInterceptor = invocation -> invocation.proceed();
        MethodInterceptor addedAfterConstruction = invocation -> "changed";
        interceptors.add(passThroughInterceptor);

        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[]{1, 2},
            interceptors
        );

        interceptors.add(addedAfterConstruction);

        // When
        Object result = invocation.proceed();

        // Then
        assertEquals(3, result);
        assertEquals(1, target.callCount);
    }

    @Test
    public void 대상_메서드의_예외를_원래_예외로_전파한다() throws Throwable {
        // Given
        ExceptionThrowingTarget target = new ExceptionThrowingTarget();
        Method method = ExceptionThrowingTarget.class.getMethod("fail");

        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[0],
            Collections.emptyList()
        );

        // When
        try {
            invocation.proceed();
        } catch (ExpectedTargetException ex) {
            // Then
            assertSame(target.exception, ex);
            return;
        }

        throw new AssertionError("Expected target exception");
    }

    @Test
    public void 인터셉터의_예외를_그대로_전파한다() throws Throwable {
        // Given
        CountingTarget target = new CountingTarget();
        Method method = CountingTarget.class.getMethod("add", int.class, int.class);
        ExpectedTargetException exception = new ExpectedTargetException();
        MethodInterceptor interceptor = invocation -> {
            throw exception;
        };

        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
            new Object(),
            target,
            method,
            new Object[]{1, 2},
            Collections.singletonList(interceptor)
        );

        // When
        try {
            invocation.proceed();
        } catch (ExpectedTargetException ex) {
            // Then
            assertSame(exception, ex);
            assertEquals(0, target.callCount);
            return;
        }

        throw new AssertionError("Expected interceptor exception");
    }

    public static class CountingTarget {

        private int callCount;

        public int add(int left, int right) {
            callCount++;
            return left + right;
        }
    }

    public static class EventRecordingTarget {

        private final List<String> executionOrder;

        public EventRecordingTarget(List<String> executionOrder) {
            this.executionOrder = executionOrder;
        }

        public String execute() {
            executionOrder.add("target");
            return "ok";
        }
    }

    public static class NoArgumentTarget {

        public String execute() {
            return "ok";
        }
    }

    public static class ExceptionThrowingTarget {

        private final ExpectedTargetException exception = new ExpectedTargetException();

        public void fail() {
            throw exception;
        }
    }

    private static class ExpectedTargetException extends RuntimeException {

    }

}
