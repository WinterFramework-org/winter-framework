package froggy.winterframework.aop.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import froggy.winterframework.aop.MethodInterceptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class JdkProxyFactoryTest {

    private final ProxyFactory proxyFactory = new JdkProxyFactory();

    @Test
    public void 인터페이스를_구현한_target이면_proxy를_생성한다() {
        // Given
        Echo target = new EchoImpl();

        // When
        Object proxy = proxyFactory.createProxy(target, Collections.emptyList());

        // Then
        assertTrue(java.lang.reflect.Proxy.isProxyClass(proxy.getClass()));
    }

    @Test
    public void 생성된_proxy는_target_interface_타입이다() {
        // Given
        Echo target = new EchoImpl();

        // When
        Object proxy = proxyFactory.createProxy(target, Collections.emptyList());

        // Then
        assertTrue(proxy instanceof Echo);
    }

    @Test
    public void 생성된_proxy는_target_구현_클래스_타입이_아니다() {
        // Given
        EchoImpl target = new EchoImpl();

        // When
        Object proxy = proxyFactory.createProxy(target, Collections.emptyList());

        // Then
        assertFalse(proxy instanceof EchoImpl);
    }

    @Test
    public void 인터페이스가_없는_target이면_예외를_던진다() {
        // Given
        NoInterfaceTarget target = new NoInterfaceTarget();

        // When
        try {
            proxyFactory.createProxy(target, Collections.emptyList());
        } catch (IllegalArgumentException ex) {
            // Then
            assertTrue(ex.getMessage().contains(NoInterfaceTarget.class.getName()));
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void 상위_클래스의_인터페이스도_proxy_인터페이스로_사용한다() {
        // Given
        ChildEchoImpl target = new ChildEchoImpl();

        // When
        Object proxy = proxyFactory.createProxy(target, Collections.emptyList());

        // Then
        assertTrue(proxy instanceof Echo);
        assertEquals("ok", ((Echo) proxy).execute("ok"));
    }

    @Test
    public void 상위_인터페이스_타입으로_proxy를_캐스팅할_수_있다() {
        // Given
        ExtendedEchoImpl target = new ExtendedEchoImpl();

        // When
        Object proxy = proxyFactory.createProxy(target, Collections.emptyList());

        // Then
        assertTrue(proxy instanceof Echo);
        assertEquals("ok", ((Echo) proxy).execute("ok"));
    }

    @Test
    public void 기본_생성자_없는_target도_proxy로_호출한다() {
        // Given
        Echo target = new NoDefaultConstructorEcho("required");

        // When
        Echo proxy = (Echo) proxyFactory.createProxy(target, Collections.emptyList());

        // Then
        assertEquals("ok", proxy.execute("ok"));
    }

    @Test
    public void proxy_호출은_interceptor를_거쳐_target을_실행한다() {
        // Given
        AtomicBoolean interceptorCalled = new AtomicBoolean();
        MethodInterceptor interceptor = invocation -> {
            interceptorCalled.set(true);
            return invocation.proceed();
        };
        Echo proxy = (Echo) proxyFactory.createProxy(
            new EchoImpl(),
            Collections.singletonList(interceptor)
        );

        // When
        Object result = proxy.execute("ok");

        // Then
        assertTrue(interceptorCalled.get());
        assertEquals("ok", result);
    }

    @Test
    public void Object_메서드는_interceptor를_거치지_않는다() {
        // Given
        AtomicInteger interceptorCallCount = new AtomicInteger();
        ObjectMethodEcho target = new ObjectMethodEcho();
        MethodInterceptor interceptor = invocation -> {
            interceptorCallCount.incrementAndGet();
            return invocation.proceed();
        };
        Object proxy = proxyFactory.createProxy(target, Collections.singletonList(interceptor));

        // When
        boolean equalsSelf = proxy.equals(proxy);
        boolean equalsOther = proxy.equals(new Object());
        int hashCode = proxy.hashCode();
        String toString = proxy.toString();

        // Then: equals
        assertTrue(equalsSelf);
        assertFalse(equalsOther);
        assertEquals(0, interceptorCallCount.get());

        // Then: hashCode
        assertEquals(System.identityHashCode(proxy), hashCode);
        assertEquals(0, interceptorCallCount.get());

        // Then: toString
        assertTrue(toString.contains(ObjectMethodEcho.class.getName()));
        assertEquals(0, interceptorCallCount.get());
    }

    @Test
    public void 생성_후_원본_interceptor_목록이_바뀌어도_proxy의_체인은_변하지_않는다() {
        // Given
        List<MethodInterceptor> interceptors = new ArrayList<>();
        interceptors.add(invocation -> invocation.proceed());
        Echo proxy = (Echo) proxyFactory.createProxy(new EchoImpl(), interceptors);

        interceptors.add(invocation -> "changed");

        // When
        Object result = proxy.execute("ok");

        // Then
        assertEquals("ok", result);
    }

    @Test
    public void target_예외를_그대로_전파한다() {
        // Given
        ExpectedTargetException expectedException = new ExpectedTargetException();
        Echo throwingTarget = value -> {
            throw expectedException;
        };
        Echo proxy = (Echo) proxyFactory.createProxy(throwingTarget, Collections.emptyList());

        // When
        try {
            proxy.execute("ok");
        } catch (ExpectedTargetException actualException) {
            // Then
            assertSame(expectedException, actualException);
            return;
        }

        throw new AssertionError("Expected target exception");
    }

    @Test
    public void null_target은_예외를_던진다() {
        // Given
        Object target = null;

        // When
        try {
            proxyFactory.createProxy(target, Collections.emptyList());
        } catch (IllegalArgumentException ex) {
            // Then
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void null_interceptors는_예외를_던진다() {
        // Given
        Echo target = new EchoImpl();

        // When
        try {
            proxyFactory.createProxy(target, null);
        } catch (IllegalArgumentException ex) {
            // Then
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void null_interceptor_element는_예외를_던진다() {
        // Given
        List<MethodInterceptor> interceptors = new ArrayList<>();
        interceptors.add(null);

        // When
        try {
            proxyFactory.createProxy(new EchoImpl(), interceptors);
        } catch (IllegalArgumentException ex) {
            // Then
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }

    public interface Echo {

        String execute(String value);
    }

    public static class EchoImpl implements Echo {

        @Override
        public String execute(String value) {
            return value;
        }
    }

    public static class NoInterfaceTarget {

    }

    public static class ChildEchoImpl extends EchoImpl {

    }

    public interface ExtendedEcho extends Echo {

    }

    public static class ExtendedEchoImpl implements ExtendedEcho {

        @Override
        public String execute(String value) {
            return value;
        }
    }

    public static class NoDefaultConstructorEcho implements Echo {

        public NoDefaultConstructorEcho(String constructorArgument) {
        }

        @Override
        public String execute(String value) {
            return value;
        }
    }

    public static class ObjectMethodEcho implements Echo {

        @Override
        public boolean equals(Object obj) {
            throw new AssertionError("target equals must not be called");
        }

        @Override
        public int hashCode() {
            throw new AssertionError("target hashCode must not be called");
        }

        @Override
        public String toString() {
            throw new AssertionError("target toString must not be called");
        }

        @Override
        public String execute(String value) {
            return value;
        }
    }

    private static class ExpectedTargetException extends RuntimeException {

    }

}
