package froggy.winterframework.web.method.support;

import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import java.lang.reflect.Parameter;

/**
 * 핸들러 메서드의 인자 값을 변환하여 주입하는 인터페이스.
 *
 * <p>메서드 파라미터를 지원하는지 확인하고, 요청에서 값을 추출하여 변환하는 기능을 제공.</p>
 */
public interface HandlerMethodArgumentResolver {

    /**
     * 주어진 파라미터를 지원하는지 여부를 확인.
     *
     * @param parameter 검사할 파라미터 객체
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    boolean supportsParameter(Parameter parameter);

    /**
     * 요청에서 파라미터 값을 추출하고 변환하여 반환.
     *
     * @param parameter 변환할 파라미터 객체
     * @param webRequest 현재 Request 컨텍스트
     * @param mavContainer 현재 요청의 Model/View 처리 상태를 관리하는 컨테이너
     * @return 변환된 인자 값
     * @throws Exception 변환 과정에서 발생하는 예외
     */
    Object resolveArgument(Parameter parameter, NativeWebRequest webRequest, ModelAndView mavContainer) throws Exception;
}
