package froggy.winterframework.validation;

/**
 * Winter 검증 엔진의 최상위 인터페이스.
 *
 * <p>컨트롤러에서 직접 호출해 객체를 검증하고,
 * 검증 오류는 {@link BindingResult}에 누적한다.</p>
 */
public interface Validator {

    /**
     * 해당 타입을 현재 검증기가 처리할 수 있는지 확인한다.
     *
     * @param targetClass 검증 대상 타입
     * @return 처리 가능하면 {@code true}
     */
    boolean supports(Class<?> targetClass);

    /**
     * 기본 그룹으로 검증한다.
     *
     * @param target 검증 대상 객체
     * @param bindingResult 검증 결과 저장소
     */
    default void validate(Object target, BindingResult bindingResult) {
        validate(target, bindingResult, new Class<?>[0]);
    }

    /**
     * 지정한 그룹으로 검증한다.
     *
     * @param target 검증 대상 객체
     * @param bindingResult 검증 결과 저장소
     * @param groups 검증 그룹
     */
    void validate(Object target, BindingResult bindingResult, Class<?>... groups);
}
