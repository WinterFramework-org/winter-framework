package froggy.winterframework.web.method.support;

import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.method.HandlerMethod;

/**
 * 핸들러 메소드의 반환 값을 처리하는 인터페이스.
 */
public interface HandlerMethodReturnValueHandler {

    /**
     * 주어진 핸들러 메소드의 반환 타입을 지원하는지 여부를 확인.
     *
     * @param handlerMethod 처리 대상의 핸들러 메소드
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    boolean supportsReturnType(HandlerMethod handlerMethod);

    /**
     * 컨트롤러 메소드에서 반환된 값을 처리하는 메소드
     *
     * @param returnValue 핸들러 메소드의 반환 값
     * @param returnType 반환 값의 클래스 타입
     * @param webRequest 현재 Request 컨텍스트
     */
    void handleReturnValue(Object returnValue, Class<?> returnType, NativeWebRequest webRequest);
}