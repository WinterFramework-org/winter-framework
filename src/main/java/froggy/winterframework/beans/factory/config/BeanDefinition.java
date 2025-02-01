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

    /**
     * 지정된 클래스 타입과 스코프를 사용하여 새로운 {@code BeanDefinition} 객체를 생성.
     *
     * @param beanClass Bean으로 생성할 클래스의 {@link Class} 객체
     * @param scope     Bean의 스코프 (e.g: "singleton")
     */
    public BeanDefinition(Class<?> beanClass, String scope) {
        this.beanClass = beanClass;
        this.scope = scope;
    }


    /**
     * 지정된 클래스 타입을 사용하여 새로운 {@code BeanDefinition} 객체를 생성,
     * 기본 스코프로는 "singleton" 사용.
     *
     * @param beanClass 빈으로 생성할 클래스의 {@link Class} 객체
     */
    public BeanDefinition(Class<?> beanClass) {
        this(beanClass, "singleton");
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

}
