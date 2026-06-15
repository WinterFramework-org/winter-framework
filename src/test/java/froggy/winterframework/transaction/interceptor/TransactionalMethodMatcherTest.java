package froggy.winterframework.transaction.interceptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import froggy.winterframework.transaction.annotation.Transactional;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

public class TransactionalMethodMatcherTest {

    private TransactionalMethodMatcher matcher;

    @Before
    public void setUp() {
        matcher = new TransactionalMethodMatcher();
    }

    @Test
    public void Transactional이_없는_method는_false를_반환한다() throws NoSuchMethodException {
        // Given
        Method method = TestService.class.getMethod("execute");

        // When
        boolean actual = matcher.matches(method, NonTransactionalService.class);

        // Then
        assertFalse(actual);
    }

    @Test
    public void 구현체_method가_Transactional이면_true를_반환한다() throws NoSuchMethodException {
        // Given
        Method method = TestService.class.getMethod("execute");

        // When
        boolean actual = matcher.matches(method, MethodTransactionalService.class);

        // Then
        assertTrue(actual);
    }

    @Test
    public void interface_method가_Transactional이면_true를_반환한다() throws NoSuchMethodException {
        // Given
        Method method = TransactionalInterfaceImpl.class.getMethod("execute");

        // When
        boolean actual = matcher.matches(method, TransactionalInterfaceImpl.class);

        // Then
        assertTrue(actual);
    }

    @Test
    public void 상위_class가_구현한_interface_method가_Transactional이면_true를_반환한다() throws NoSuchMethodException {
        // Given
        Method method = ChildTransactionalInterfaceImpl.class.getMethod("execute");

        // When
        boolean actual = matcher.matches(method, ChildTransactionalInterfaceImpl.class);

        // Then
        assertTrue(actual);
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

    public static class BaseTransactionalInterfaceImpl implements TransactionalInterface {

        @Override
        public String execute() {
            return "ok";
        }
    }

    public static class ChildTransactionalInterfaceImpl extends BaseTransactionalInterfaceImpl {
    }
}
