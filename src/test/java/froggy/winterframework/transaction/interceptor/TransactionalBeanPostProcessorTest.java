package froggy.winterframework.transaction.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import froggy.winterframework.aop.framework.JdkProxyFactory;
import froggy.winterframework.transaction.annotation.Transactional;
import java.lang.reflect.Proxy;
import org.junit.Test;

public class TransactionalBeanPostProcessorTest {

    private final TransactionalBeanPostProcessor postProcessor =
        new TransactionalBeanPostProcessor(new JdkProxyFactory(), new TransactionInterceptor());

    @Test
    public void Transactional_method가_있는_bean은_JDK_proxy로_등록된다() {
        // Given
        TransactionalServiceImpl target = new TransactionalServiceImpl();

        // When
        Object bean = postProcessor.postProcessAfterInitialization(target, "transactionalService");

        // Then
        assertTrue(Proxy.isProxyClass(bean.getClass()));
        assertTrue(bean instanceof TransactionalService);
    }

    @Test
    public void proxy_method_호출은_target_method를_실행한다() {
        // Given
        TransactionalServiceImpl target = new TransactionalServiceImpl();
        TransactionalService proxy = (TransactionalService) postProcessor
            .postProcessAfterInitialization(target, "transactionalService");

        // When
        String result = proxy.execute();

        // Then
        assertEquals("ok", result);
        assertEquals(1, target.callCount);
    }

    @Test
    public void Transactional_method가_없는_bean은_원본을_유지한다() {
        // Given
        PlainServiceImpl target = new PlainServiceImpl();

        // When
        Object bean = postProcessor.postProcessAfterInitialization(target, "plainService");

        // Then
        assertSame(target, bean);
    }

    @Test
    public void interface가_없는_Transactional_bean은_예외를_던진다() {
        // Given
        NoInterfaceTransactionalService target = new NoInterfaceTransactionalService();

        // When
        try {
            postProcessor.postProcessAfterInitialization(target, "noInterfaceTransactionalService");
        } catch (IllegalArgumentException ex) {
            // Then
            assertTrue(ex.getMessage().contains(NoInterfaceTransactionalService.class.getName()));
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }

    public interface TransactionalService {

        String execute();
    }

    public static class TransactionalServiceImpl implements TransactionalService {

        private int callCount;

        @Override
        @Transactional
        public String execute() {
            callCount++;
            return "ok";
        }
    }

    public interface PlainService {

        String execute();
    }

    public static class PlainServiceImpl implements PlainService {

        @Override
        public String execute() {
            return "ok";
        }
    }

    public static class NoInterfaceTransactionalService {

        @Transactional
        public void execute() {
        }
    }
}
