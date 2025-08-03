package froggy.winterframework.web.method.annotation;

/**
 * 어노테이션 속성에서 파라미터의 필수 여부를 나타내는 데 사용되는 상수.
 * <p>사용자가 명시적으로 값을 제공해야 하는지, 선택적으로 제공해도 되는지를 구분할 때 사용.
 */
public interface RequiredConstants {
    /**
     * 파라미터가 필수임을 나타내는 플래그.
     * <p>값이 반드시 제공되어야 함을 의미.
     */
    boolean MANDATORY = true;

    /**
     * 파라미터가 선택적임을 나타내는 플래그.
     * <p>요청 파라미터 바인딩이 선택적이며, 값이 없을 경우 {@code null}로 처리됨.
     */
    boolean OPTIONAL = false;
}
