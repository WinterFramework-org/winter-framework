package froggy.winterframework.context.annotation;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.utils.WinterUtils;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Configuration 설정 클래스 내부를 스캔하는 PostProcessor.
 * - @Bean 메서드를 스캔해 반환 객체를 Bean으로 관리
 * - @Component 중첩된 클래스를 스캔해 Bean으로 관리
 */
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanFactory beanFactory) {
        Set<Class<?>> configCandidates = findConfigurationCandidates(beanFactory);
        HashMap<String, BeanDefinition> beanDefinitions = createBeanDefinitions(configCandidates);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition bd = entry.getValue();

            beanFactory.registerBeanDefinition(beanName, bd);
        }
    }

    private Set<Class<?>> findConfigurationCandidates(BeanFactory beanFactory) {
        Set<Class<?>> result = new LinkedHashSet<>();

        result.addAll(WinterUtils.scanTypesAnnotatedWith(
            Component.class,
            beanFactory.resolveEmbeddedValue("basePackage")
        ));

        result.addAll(WinterUtils.scanTypesAnnotatedWith(
            Component.class,
            "froggy.winterframework"
        ));
        return result;
    }

    /**
     * @Configuration 클래스 내부를 스캔해 Bean 등록 대상들을 BeanDefinition으로 생성
     */
    private HashMap<String, BeanDefinition> createBeanDefinitions(Set<Class<?>> configurationClasses) {
        HashMap<String, BeanDefinition> result = new HashMap<>();
        for (Class<?> configClass : configurationClasses) {
            result.putAll(scanFactoryMethods(configClass));
            result.putAll(scanNestedComponentClasses(configClass));
        }

        return result;
    }

    /**
     * @Bean 어노테이션이 붙은 FactoryMethod를 스캔하여 BeanDefinition 으로 변환
     */
    private HashMap<String, BeanDefinition> scanFactoryMethods(Class<?> configClass) {
        HashMap<String, BeanDefinition> result = new HashMap<>();

        String configBeanName = WinterUtils.resolveSimpleBeanName(configClass);
        for (Method method : configClass.getMethods()) {
            if (WinterUtils.hasAnnotation(method, Bean.class)) {
                BeanDefinition bd = new BeanDefinition(
                    method.getReturnType(),
                    configBeanName,
                    method.getName()
                );

                result.put(method.getName(), bd);
            }
        }

        return result;
    }

    /**
     * @Component 어노테이션이 붙은 중첩 클래스를 스캔하여 BeanDefinition 으로 변환
     */
    private HashMap<String, BeanDefinition> scanNestedComponentClasses(Class<?> configClass) {
        HashMap<String, BeanDefinition> result = new HashMap<>();

        for (Class<?> nestedClass : configClass.getDeclaredClasses()) {
            if (WinterUtils.hasAnnotation(nestedClass, Component.class)) {
                BeanDefinition bd = new BeanDefinition(nestedClass);

                result.put(WinterUtils.resolveSimpleBeanName(nestedClass), bd);
            }
        }

        return result;
    }
}
