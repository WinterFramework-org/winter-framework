package froggy.winterframework.context.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import froggy.winterframework.beans.factory.support.BeanFactory;
import java.lang.reflect.Method;
import java.util.Collections;
import org.junit.Test;

public class ConfigurationClassPostProcessorTest {

    @Test
    public void 일반_Bean을_등록하고_생성한다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.registerBeanDefinition(PlainConfiguration.class);

        // When
        new ConfigurationClassPostProcessor().postProcessBeanDefinitionRegistry(beanFactory);

        // Then
        assertEquals("plain", beanFactory.getBean("widget", String.class));
    }

    @Test
    public void 서로_다른_Bean의_이름이_같으면_등록을_거부한다() {
        // Given
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.registerBeanDefinition(PlainConfiguration.class);
        beanFactory.registerBeanDefinition(OtherConfiguration.class);

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> new ConfigurationClassPostProcessor().postProcessBeanDefinitionRegistry(beanFactory));

        // Then
        assertEquals("Duplicate BeanDefinition name: widget", exception.getMessage());
    }

    @Test // gh-24
    public void bridge가_있는_Bean을_중복_없이_등록하고_생성한다() {
        // Given
        int beanBridgeCount = 0;
        for (Method method : CovariantConfiguration.class.getDeclaredMethods()) {
            if (method.isBridge() && method.isAnnotationPresent(Bean.class)) {
                beanBridgeCount++;
            }
        }
        assertEquals(1, beanBridgeCount);

        BeanFactory beanFactory = new BeanFactory();
        beanFactory.registerBeanDefinition(CovariantConfiguration.class);

        // When
        new ConfigurationClassPostProcessor().postProcessBeanDefinitionRegistry(beanFactory);

        // Then
        assertEquals(Integer.class, beanFactory.getBeanDefinition("number").getBeanClass());
        assertEquals(Integer.valueOf(42), beanFactory.getBean("number", Integer.class));
        assertEquals(Collections.singletonList("number"), beanFactory.getBeanNamesForType(Number.class));
    }

    // 일반 테스트용 설정
    @Configuration
    public static class PlainConfiguration {
        @Bean
        public String widget() {
            return "plain";
        }
    }

    @Configuration
    public static class OtherConfiguration {
        @Bean
        public String widget() {
            return "other";
        }
    }

    // gh-24 재현용 설정
    public static class ParentConfiguration {
        public Number number() {
            return 10;
        }
    }

    @Configuration
    public static class CovariantConfiguration extends ParentConfiguration {
        @Override
        @Bean
        public Integer number() {
            return 42;
        }
    }
}
