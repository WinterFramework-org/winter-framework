package froggy.winterframework.beans.factory.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public class BeanFactoryBeanPostProcessorTest {

    @Test
    public void BeanPostProcessor가_없으면_생성된_bean을_그대로_singleton으로_등록한다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.registerBeanDefinition("sampleBean", new BeanDefinition(SampleBean.class));

        // When
        Object firstBean = beanFactory.getBean("sampleBean");
        Object secondBean = beanFactory.getBean("sampleBean");

        // Then
        assertTrue(firstBean instanceof SampleBean);
        assertSame(firstBean, secondBean);
    }

    @Test
    public void BeanPostProcessor가_반환한_객체를_singleton으로_등록한다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();
        SampleBean processedBean = new SampleBean();
        beanFactory.registerBeanDefinition("sampleBean", new BeanDefinition(SampleBean.class));
        beanFactory.addBeanPostProcessor((bean, beanName) -> processedBean);

        // When
        Object firstBean = beanFactory.getBean("sampleBean");
        Object secondBean = beanFactory.getBean("sampleBean");

        // Then
        assertSame(processedBean, firstBean);
        assertSame(processedBean, secondBean);
    }

    @Test
    public void 여러_BeanPostProcessor는_순서대로_bean을_처리한다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();
        SampleBean firstProcessorResult = new SampleBean();
        SampleBean secondProcessorResult = new SampleBean();
        AtomicReference<Object> beanPassedToSecondProcessor = new AtomicReference<>();
        beanFactory.registerBeanDefinition("sampleBean", new BeanDefinition(SampleBean.class));
        beanFactory.addBeanPostProcessor((bean, beanName) -> firstProcessorResult);
        beanFactory.addBeanPostProcessor((bean, beanName) -> {
            beanPassedToSecondProcessor.set(bean);
            return secondProcessorResult;
        });

        // When
        Object result = beanFactory.getBean("sampleBean");

        // Then
        assertSame(firstProcessorResult, beanPassedToSecondProcessor.get());
        assertSame(secondProcessorResult, result);
    }

    @Test
    public void BeanPostProcessor에_beanName을_전달한다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();
        AtomicReference<String> beanNamePassedToPostProcessor = new AtomicReference<>();
        beanFactory.registerBeanDefinition("sampleBean", new BeanDefinition(SampleBean.class));
        beanFactory.addBeanPostProcessor((bean, beanName) -> {
            beanNamePassedToPostProcessor.set(beanName);
            return bean;
        });

        // When
        beanFactory.getBean("sampleBean");

        // Then
        assertEquals("sampleBean", beanNamePassedToPostProcessor.get());
    }

    @Test
    public void null_BeanPostProcessor는_예외를_던진다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();

        // When
        try {
            beanFactory.addBeanPostProcessor(null);
        } catch (IllegalArgumentException ex) {
            // Then
            assertTrue(ex.getMessage().contains("BeanPostProcessor"));
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void BeanPostProcessor가_null을_반환하면_예외를_던지고_singleton으로_등록하지_않는다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.registerBeanDefinition("sampleBean", new BeanDefinition(SampleBean.class));
        beanFactory.addBeanPostProcessor((bean, beanName) -> null);

        // When
        try {
            beanFactory.getBean("sampleBean");
        } catch (IllegalStateException ex) {
            // Then
            assertTrue(ex.getMessage().contains("sampleBean"));
            assertNull(beanFactory.getSingleton("sampleBean"));
            return;
        }

        throw new AssertionError("Expected IllegalStateException");
    }

    public static class SampleBean {
    }
}
