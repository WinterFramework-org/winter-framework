package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import java.lang.reflect.Parameter;
import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 요청의 매개변수를 추출하고 변환하는 abstract 클래스.
 *
 * <p>직접적인 타입 변환은 {@code TypeConverter}에서 수행하며,
 * 하위 클래스는 {@link #extractValue(Parameter, HttpServletRequest)}와
 * {@link #supportsParameter(Parameter)}를 오버라이드하여 구현</p>

 */
public abstract class AbstractMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final TypeConverter converter;

    public AbstractMethodArgumentResolver(TypeConverter converter) {
        this.converter = converter;
    }

    /**
     * 주어진 HTTP 요청에서 매개변수를 추출하고 변환하여 반환한다.
     *
     * @param parameter 변환할 매개변수 객체
     * @param request   HTTP 요청 객체
     * @return 변환된 매개변수 값
     * @throws Exception 변환 과정에서 발생하는 예외
     */
    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request) throws Exception {
        String value = extractValue(parameter, request);
        Class<?> targetType = parameter.getType();

        try {
            return converter.convert(targetType, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to convert request parameter '" + parameter.getName() + "' with value '" + value + "' to type '" + targetType.getSimpleName() + "'.", e);
        }
    }

    /**
     * HTTP 요청에서 주어진 매개변수의 값을 추출하는 추상 메서드.
     * <p>구현 클래스에서 해당 로직을 정의해야 한다.</p>
     *
     * @param parameter 추출할 매개변수 객체
     * @param request   HTTP 요청 객체
     * @return 요청에서 추출한 문자열 값 (없을 경우 {@code null} 반환)
     */
    protected abstract String extractValue(Parameter parameter, HttpServletRequest request);
}
