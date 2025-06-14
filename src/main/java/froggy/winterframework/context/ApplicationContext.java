package froggy.winterframework.context;

import froggy.winterframework.beans.factory.config.BeanFactoryPostProcessor;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.core.env.Environment;
import java.util.ArrayList;
import java.util.List;

/**
 * ({@link BeanFactory})를 포함하며, Bean을 조회하고 관리하는 역할.
 *
 * {@code ApplicationContext}는 프레임워크의 Bean 컨테이너로서,
 * <p>내부적으로 {@code BeanFactory}를 포함(Has-A)하여 빈의 생성, 조회, 초기화를 담당
 */
public class ApplicationContext {

    /** 애플리케이션의 Bean 관리를 담당하는 팩토리 */
    private final BeanFactory beanFactory;

    /**
     * 애플리케이션 컨텍스트 초기화 시 실행할 BeanFactoryPostProcessor 목록
     */
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors;

    /** 애플리케이션 전체 설정(properties, 환경변수 등)을 제공하는 Environment */
    private Environment environment;

    public ApplicationContext() {
        beanFactory = new BeanFactory();
        beanFactoryPostProcessors = new ArrayList<>();
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void addEnvironment(Environment environment) {
        this.environment = environment;
    }

    public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return beanFactoryPostProcessors;
    }

    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
        beanFactoryPostProcessors.add(postProcessor);
    }

    /**
     * 특정 Bean을 조회하여 해당객체 반환.
     *
     * <p>Bean 이름을 기반으로 {@link BeanFactory}에서 Bean을 검색하고 반환.
     * Bean이 존재하지 않으면 생성해서 반환
     *
     * @param beanName 조회할 빈의 이름
     * @return Bean 객체
     */
    public Object getBean(String beanName) {
        return getBeanFactory().getBean(beanName);
    }

}
