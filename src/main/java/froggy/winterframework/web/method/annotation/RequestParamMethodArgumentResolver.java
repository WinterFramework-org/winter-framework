package froggy.winterframework.web.method.annotation;

import froggy.winterframework.core.MethodParameter;
import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.context.request.NativeWebRequest;

/**
 * {@link RequestParam} 어노테이션을 처리하는 Argument Resolver.
 * <p>HTTP 요청 파라미터(쿼리 스트링 또는 폼 데이터)에서 값을 추출합니다.
 */
public class RequestParamMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public RequestParamMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class);
    }

    @Override
    protected String extractValue(MethodParameter parameter, NativeWebRequest webRequest) {
        String paramName = parameter.getParameterAnnotation(RequestParam.class).value();
        return webRequest.getParameter(paramName);
    }

    /**
     * {@link RequestParam} 어노테이션 정보를 기반으로 {@link NamedValueInfo}를 생성.
     */
    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);

        return new NamedValueInfo(ann.value(), ann.required(), ann.defaultValue());
    }
}
