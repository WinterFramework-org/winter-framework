package froggy.winterframework.context.annotation;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.beans.factory.config.ScopeType;
import froggy.winterframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.utils.WinterUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Configuration 설정 클래스 내부를 스캔하는 PostProcessor.
 * - @Bean 메서드를 스캔해 반환 객체를 Bean으로 관리
 * - @Component 중첩된 클래스를 스캔해 Bean으로 관리
 */
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private final AnnotationScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanFactory beanFactory) {
        List<ConfigurationCandidate> configCandidates = findConfigurationCandidates(beanFactory);
        HashMap<String, BeanDefinition> beanDefinitions = createBeanDefinitions(configCandidates);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition bd = entry.getValue();

            beanFactory.registerBeanDefinition(beanName, bd);
        }
    }

    private List<ConfigurationCandidate> findConfigurationCandidates(BeanFactory beanFactory) {
        List<ConfigurationCandidate> result = new ArrayList<>();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            // FactoryMethod 기반 BeanDefinition은 설정 클래스 후보가 아니므로 제외한다.
            if (beanDefinition.getFactoryMethodName() != null) {
                continue;
            }

            Class<?> beanClass = beanDefinition.getBeanClass();
            if (WinterUtils.hasAnnotation(beanClass, Component.class)) {
                result.add(new ConfigurationCandidate(beanName, beanClass));
            }
        }

        return result;
    }

    /**
     * @Configuration 클래스 내부를 스캔해 Bean 등록 대상들을 BeanDefinition으로 생성한다.
     */
    private HashMap<String, BeanDefinition> createBeanDefinitions(List<ConfigurationCandidate> configurationCandidates) {
        HashMap<String, BeanDefinition> result = new HashMap<>();
        for (ConfigurationCandidate candidate : configurationCandidates) {
            addBeanDefinitionsForBeanMethods(result, candidate);
            addBeanDefinitionsForNestedComponentClasses(result, candidate.beanClass);
        }

        return result;
    }

    /**
     * @Bean 메서드로 선언된 BeanDefinition 후보를 추가한다.
     */
    private void addBeanDefinitionsForBeanMethods(
        Map<String, BeanDefinition> target,
        ConfigurationCandidate candidate
    ) {
        for (Method method : candidate.beanClass.getMethods()) {
            if (WinterUtils.hasAnnotation(method, Bean.class)) {
                ScopeType scopeType = scopeMetadataResolver.resolveScopeMetadata(method);
                BeanDefinition bd = new BeanDefinition(
                    method.getReturnType(),
                    scopeType,
                    candidate.beanName,
                    method.getName()
                );

                addBeanDefinition(target, method.getName(), bd);
            }
        }
    }

    /**
     * @Component 중첩 클래스로 선언된 BeanDefinition 후보를 추가한다.
     */
    private void addBeanDefinitionsForNestedComponentClasses(
        Map<String, BeanDefinition> target,
        Class<?> configClass
    ) {
        for (Class<?> nestedClass : configClass.getDeclaredClasses()) {
            if (WinterUtils.hasAnnotation(nestedClass, Component.class)) {
                BeanDefinition bd = new BeanDefinition(
                    nestedClass,
                    scopeMetadataResolver.resolveScopeMetadata(nestedClass)
                );

                addBeanDefinition(target, WinterUtils.resolveSimpleBeanName(nestedClass), bd);
            }
        }
    }

    /**
     * CCPP가 생성한 BeanDefinition 후보를 추가한다.
     */
    private void addBeanDefinition(Map<String, BeanDefinition> target, String beanName, BeanDefinition beanDefinition) {
        if (target.containsKey(beanName)) {
            throw new IllegalStateException("Duplicate BeanDefinition name: " + beanName);
        }

        target.put(beanName, beanDefinition);
    }

    private static class ConfigurationCandidate {
        private final String beanName;
        private final Class<?> beanClass;

        private ConfigurationCandidate(String beanName, Class<?> beanClass) {
            this.beanName = beanName;
            this.beanClass = beanClass;
        }
    }
}
