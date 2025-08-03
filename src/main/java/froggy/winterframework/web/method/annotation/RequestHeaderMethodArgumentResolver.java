package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.RequestHeader;
import java.lang.reflect.Parameter;
import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 요청의 Header 값을 추출하여 적절한 타입으로 변환하는 Resolver.
 *
 * 타입 변환은 상위 {@code TypeConverter}에서 진행하며,
 * 해당 클래스에서는 supportsParameter 및 extractValue를 재정의하여 사용
 */
public class RequestHeaderMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public RequestHeaderMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    /**
     * 주어진 파라매터를 지원하는지 여부를 확인.
     *
     * @param parameter 검사할 파라매터 객체
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestHeader.class);
    }

    /**
     * HTTP Request Header에서 특정 헤더의 값을 추출한다.
     *
     * @param parameter 추출할 대상 헤더와 매핑된 파라매터 객체
     * @param request   HTTP 요청 객체
     * @return HTTP Request Header에서 추출한 헤더 값 (없을 경우 {@code null} 반환)
     */
    @Override
    protected String extractValue(Parameter parameter, HttpServletRequest request) {
        String name = parameter.getAnnotation(RequestHeader.class).value();

        return request.getHeader(name);
    }

    /**
     * {@link RequestHeader} 어노테이션 정보를 기반으로 {@link NamedValueInfo}를 생성.
     */
    @Override
    protected NamedValueInfo createNamedValueInfo(Parameter parameter) {
        RequestHeader ann = parameter.getAnnotation(RequestHeader.class);

        return new NamedValueInfo(ann.value(), ann.required(), ann.defaultValue());
    }
}