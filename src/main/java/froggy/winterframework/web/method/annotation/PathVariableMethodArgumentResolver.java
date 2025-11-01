package froggy.winterframework.web.method.annotation;

import static froggy.winterframework.web.context.request.NativeWebRequest.SCOPE_REQUEST;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.PathVariable;
import froggy.winterframework.web.bind.annotation.ValueConstants;
import froggy.winterframework.web.context.request.NativeWebRequest;
import java.lang.reflect.Parameter;
import java.util.HashMap;

/**
 * HTTP 요청 URI에서 {@link PathVariable} 어노테이션이 적용 된 매개변수의 값을 추출하여 변환하는 Resolver
 *
 * <p> HTTP 요청의 URI 템플릿 변수에서 대응하는 값을 추출하고, 요구하는 타입으로 반환 </p>
 * <p> 타입 변환은 {@link AbstractMethodArgumentResolver}를 통해 진행 </p>
 */
public class PathVariableMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public PathVariableMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    /**
     * 주어진 파라미터에 @PathVariable 어노테이션이 존재하는지 확인.
     *
     * @param parameter 검사할 파라미터 객체
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(PathVariable.class);
    }

    /**
     * HTTP 요청에서 URI 템플릿 변수로부터 파라매터에 해당하는 값을 추출
     *
     * @param parameter {@code @PathVariable}이 적용된 매개변수
     * @param webRequest 현재 Request 컨텍스트
     * @return 매핑된 값, 없으면 {@code null} 반환
     */
    @Override
    protected String extractValue(Parameter parameter, NativeWebRequest webRequest) {
        String paramName = parameter.getAnnotation(PathVariable.class).value();
        HashMap<String, String> map = (HashMap<String, String>) webRequest.getAttribute("uriTemplateVariables", SCOPE_REQUEST);

        return map.get(paramName);
    }

    /**
     * {@link PathVariable} 어노테이션 정보를 기반으로 {@link NamedValueInfo}를 생성.
     */
    @Override
    protected NamedValueInfo createNamedValueInfo(Parameter parameter) {
        PathVariable ann = parameter.getAnnotation(PathVariable.class);

        return new NamedValueInfo(ann.value(), RequiredConstants.MANDATORY, ValueConstants.DEFAULT_NONE);
    }
}
