package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.ValueConstants;
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
     * @throws IllegalStateException 변환 과정에서 발생하는 예외
     */
    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request) throws IllegalStateException {
        NamedValueInfo namedValueInfo = createNamedValueInfo(parameter);

        String extractValue = extractValue(parameter, request);
        Class<?> targetType = parameter.getType();

        String resolvedValue = getValueOrDefault(extractValue, namedValueInfo);
        if (resolvedValue == null) {
            return null;
        }

        try {
            return converter.convert(targetType, resolvedValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to convert request parameter '" + namedValueInfo.name + "' with value '" + resolvedValue + "' to type '" + targetType.getSimpleName() + "'.", e);
        }
    }


    /**
     * namedValueInfo 따라 값 반환 또는 예외 처리
     * <ol>
     *   <li>extractValue가 있으면 그대로 반환</li>
     *   <li>값이 없고 defaultValue가 있으면 defaultValue 반환</li>
     *   <li>required이면서 값이 없으면 IllegalStateException 발생</li>
     * </ol>
     *
     * @param extractValue   HTTP 요청에서 추출된 원본 값
     * @param namedValueInfo 요청 파라미터 어노테이션 정보(defaultValue, required)
     * @return extractValue 또는 defaultValue, required=false면 null 반환 가능
     * @throws IllegalStateException required=true인데 값이 없을 경우
     */
    private String getValueOrDefault(String extractValue, NamedValueInfo namedValueInfo) {
        if (extractValue != null) {
            return extractValue;
        }
        if (namedValueInfo.defaultValue != null && !ValueConstants.DEFAULT_NONE.equals(namedValueInfo.defaultValue)) {
            return namedValueInfo.defaultValue;
        }
        if (namedValueInfo.required) {
            throw new IllegalStateException("Required parameter '" + namedValueInfo.name + "' is missing");
        }
        // required=false && defaultValue==null && extractValue==null
        return null;
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

    /**
     * 주어진 매개변수에서 이름, 필수 여부, 기본값 정보를 담은 {@link NamedValueInfo} 객체를 생성한다.
     *
     * @param parameter 정보를 추출할 매개변수
     * @return {@link NamedValueInfo} 객체
     */
    protected abstract NamedValueInfo createNamedValueInfo(Parameter parameter);

    /**
     * 매개변수의 이름, 필수 여부, 기본값 정보를 담는 클래스.
     */
    protected static class NamedValueInfo {
        private final String name;
        private final boolean required;
        private final String defaultValue;

        /**
         * @param name         매개변수 이름
         * @param required     필수 여부
         * @param defaultValue 기본값
         */
        public NamedValueInfo(String name, boolean required, String defaultValue) {
            this.name = name;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }
}