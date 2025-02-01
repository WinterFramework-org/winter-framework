package froggy.winterframework.beans.factory;

/**
 * 빈의 초기화 로직을 정의하는 인터페이스.
 *
 * <p>이 인터페이스를 구현하면 컨테이너가 빈 생성 이후 초기화 과정을 위임받아
 * 필요한 설정이나 추가 초기화 작업을 수행.
 * 구체적인 로직은 {@code afterPropertiesSet()} 를 Override하여 로직을 구현.
 */
public interface InitializingBean {

    /**
     * Bean 생성 후 컨테이너에 의해 호출.
     *
     * @throws Exception 실행 과정에서 발생하는 예외
     */
    void afterPropertiesSet() throws Exception;

}
