package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.RequestParam;
import java.lang.reflect.Parameter;
import javax.servlet.http.HttpServletRequest;

/**
 * {@link RequestParam} 어노테이션을 처리하는 Argument Resolver.
 * <p>HTTP 요청 파라미터(쿼리 스트링 또는 폼 데이터)에서 값을 추출합니다.
 */
public class RequestParamMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public RequestParamMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestParam.class);
    }

    @Override
    protected String extractValue(Parameter parameter, HttpServletRequest request) {
        String paramName = parameter.getAnnotation(RequestParam.class).value();
        return request.getParameter(paramName);
    }

    /**
     * {@link RequestParam} 어노테이션 정보를 기반으로 {@link NamedValueInfo}를 생성.
     */
    @Override
    protected NamedValueInfo createNamedValueInfo(Parameter parameter) {
        RequestParam ann = parameter.getAnnotation(RequestParam.class);

        return new NamedValueInfo(ann.value(), ann.required(), ann.defaultValue());
    }
}
