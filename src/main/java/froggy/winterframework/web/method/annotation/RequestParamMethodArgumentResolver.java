package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.RequestParam;
import java.lang.reflect.Parameter;
import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 요청의 Query Parameter, Form Data를 추출하여 적절한 타입으로 변환하는 Resolver.
 *
 * 타입 변환은 상위 {@code TypeConverter}에서 진행하며,
 * 해당 클래스에서는 supportsParameter 및 extractValue를 재정의하여 사용
 */
public class RequestParamMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public RequestParamMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    /**
     * 주어진 파라미터를 지원하는지 여부를 확인.
     *
     * @param parameter 검사할 파라미터 객체
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestParam.class);
    }

    /**
     * HTTP Request에서 주어진 매개변수의 값을 추출한다.
     *
     * @param parameter 추출할 매개변수 객체
     * @param request   HTTP 요청 객체
     * @return HTTP Request에서 추출한 문자열 값 (없을 경우 {@code null} 반환)
     */
    @Override
    protected String extractValue(Parameter parameter, HttpServletRequest request) {
        String paramName = parameter.getAnnotation(RequestParam.class).value();
        return request.getParameter(paramName);
    }
}
