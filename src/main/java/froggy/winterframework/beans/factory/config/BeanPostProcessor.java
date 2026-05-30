package froggy.winterframework.beans.factory.config;

/**
 * Bean 생성 이후, 최종 Bean으로 노출되기 전에 후처리하는 확장 지점.
 */
public interface BeanPostProcessor {

    /**
     * Bean 인스턴스 생성 이후, 싱글톤 등록 전에 후처리한다.
     *
     * @param bean 생성된 Bean 인스턴스
     * @param beanName Bean 이름
     * @return 최종 노출할 Bean 인스턴스
     */
    Object postProcessAfterInitialization(Object bean, String beanName);
}
