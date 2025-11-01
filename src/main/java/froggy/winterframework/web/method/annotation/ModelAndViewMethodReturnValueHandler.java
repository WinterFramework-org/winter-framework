package froggy.winterframework.web.method.annotation;

import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;

/**
 * 핸들러 메서드의 반환 타입이 {@link ModelAndView}인 경우를 처리
 */
public class ModelAndViewMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    /**
     * 반환 타입을 지원하는지 여부를 확인.
     *
     * @param handlerMethod 처리 대상의 핸들러 메소드
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supportsReturnType(HandlerMethod handlerMethod) {
        return handlerMethod.getReturnType().isAssignableFrom(ModelAndView.class);
    }

    /**
     * ModelAndView 반환 값을 처리하는 로직.
     *
     * @param returnValue 핸들러 메소드의 반환 값
     * @param returnType 반환 값의 클래스 타입
     * @param webRequest 현재 Request 컨텍스트
     */
    @Override
    public void handleReturnValue(Object returnValue, Class<?> returnType, NativeWebRequest webRequest) {
        // ModelAndView 처리는 DispatcherServlet의 render() 메서드에서 진행
    }
}
