package froggy.winterframework.beans.factory.config;

import froggy.winterframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import froggy.winterframework.beans.factory.support.BeanFactory;

/**
 * 확정된 BeanDefinition의 메타데이터를 변경하는 역할의 PostProcessor
 */
public interface BeanFactoryPostProcessor {

    /**
     * {@link BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(BeanFactory)} 이후 호출
     * 확정된 BeanDefinition의 메타데이터를 변경하는 역할을 수행
     *
     * @param beanFactory 초기화된 BeanFactory 객체
     */
    void postProcessBeanFactory(BeanFactory beanFactory);
}
