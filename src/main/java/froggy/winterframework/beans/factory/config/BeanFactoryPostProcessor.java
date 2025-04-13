package froggy.winterframework.beans.factory.config;

import froggy.winterframework.beans.factory.support.BeanFactory;

/**
 * Bean 생명주기 초기 단계에서 BeanDefinition을 수정할 수 있는 확장 포인트
 *
 * 모든 BeanDefinition이 로드된 후, Bean 인스턴스가 생성되기 전에 실행됨
 *
 * 이미 로드된 BeanDefinition의 메타데이터 수정
 */
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(BeanFactory beanFactory);
}
