package froggy.winterframework.beans.factory.config;

/**
 * Bean의 메타데이터를 저장하는 클래스 (Bean의 정의 정보 관리)
 *
 * <p>Bean의 클래스 타입, 스코프 등의 속성을 정의하며,
 * {@link BeanDefinition}를 기반으로 {@code BeanFactory} 에서 Bean을 생성할 때 참고.
 */
public class BeanDefinition {

    /** Bean의 클래스 타입을 나타내는 {@link Class} 객체. */
    private Class<?> beanClass;

    /** Bean의 스코프를 나타내는 문자열 (e.g: "singleton") */
    private String scope;

    /** 인스턴스 생성을 담당할 Factory Bean(설정 클래스 인스턴스)의 이름 */
    private String factoryBeanName;

    /** 호출할 FactoryMethod의 이름. */
    private String factoryMethodName;


    public BeanDefinition(Class<?> beanClass) {
        this(beanClass, "singleton");
    }

    public BeanDefinition(Class<?> beanClass, String scope) {
        this(beanClass, scope, null, null);
    }

    public BeanDefinition(Class<?> beanClass, String factoryBeanName, String factoryMethodName) {
        this(beanClass,"singleton", factoryBeanName, factoryMethodName);
    }

    /**
     * @param beanClass         Bean으로 생성할 클래스의 {@link Class} 객체
     * @param scope             Bean의 스코프
     * @param factoryBeanName   인스턴스 생성에 사용할 Factory Bean의 이름
     * @param factoryMethodName 호출할 FactoryMethod 이름
     */
    public BeanDefinition(Class<?> beanClass, String scope, String factoryBeanName, String factoryMethodName) {
        this.beanClass = beanClass;
        this.scope = scope;
        this.factoryBeanName = factoryBeanName;
        this.factoryMethodName = factoryMethodName;
    }

    /**
     * 빈의 클래스 타입을 반환.
     *
     * @return 빈의 클래스 타입을 나타내는 {@link Class} 객체
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * 빈의 스코프를 반환.
     *
     * @return 빈의 스코프 (e.g: "singleton")
     */
    public String getScope() {
        return scope;
    }

    /**
     * @return 인스턴스 생성을 담당할 Factory Bean(설정 클래스 인스턴스)의 이름
     */
    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    /**
     * @return 호출할 FactoryMethod의 이름
     */
    public String getFactoryMethodName() {
        return factoryMethodName;
    }
}
