package froggy.winterframework.beans.factory.support;

import froggy.winterframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * 빈 정의(BeanDefinition)를 등록·변경할 수 있는 역할의 PostProcessor
 *
 * 모든 BeanDefinition이 로드된 후 {@link #postProcessBeanFactory} 보다 먼저 실행됨
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

    /**
     * BeanDefinition 목록이 확정되기 전, BeanDefinition 추가·수정·삭제할 역할을 수행
     *
     * @param beanFactory BeanFactory
     */
    void postProcessBeanDefinitionRegistry(BeanFactory beanFactory);

    @Override
    default void postProcessBeanFactory(BeanFactory beanFactory) {
    }
}