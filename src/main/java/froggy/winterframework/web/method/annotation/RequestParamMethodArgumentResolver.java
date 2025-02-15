package froggy.winterframework.web.method.annotation;

import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 요청의 Query Parameter나 Form Data를 추출하여 적절한 타입으로 변환하는 Resolver
 *
 * <p>지원하는 타입</p>
 * <ul>
 *     <li>기본형 타입 (Primitive Types): {@code int, long, boolean, short, float, double, byte, char}</li>
 *     <li>래퍼 타입 (Wrapper Types): {@code Integer, Long, Boolean, Short, Float, Double, Byte, Character}</li>
 *     <li>{@code String} 타입</li>
 * </ul>
 */
public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {

    /* 문자열을 특정 타입으로 변환하는 Converter */
    private static final Map<Class<?>, Function<String, ?>> CONVERTERS = new HashMap<>();

    static {
        initializeConverters();
    }

    /**
     * 주어진 파라미터를 지원하는지 여부를 확인.
     *
     * @param parameter 검사할 파라미터 객체
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supportsParameter(Parameter parameter) {
        return CONVERTERS.containsKey(parameter.getType());
    }

    /**
     * HTTP 요청에서 파라미터 값을 추출하고 변환하여 반환.
     *
     * <p>요청의 Query Parameter, Form Data에서 값을 추출하여
     * 메서드의 매개변수의 타입으로 변환 역할을 수행</p>
     *
     * @param parameter 변환할 파라미터 객체
     * @param request   HTTP 요청 객체
     * @return 변환된 인자 값
     * @throws IllegalStateException 변환 실패 시 발생
     */
    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request) throws Exception {
        String paramName = parameter.getAnnotation(RequestParam.class).value();
        String value = request.getParameter(paramName);
        Class<?> targetType = parameter.getType();

        Function<String, ?> converter = CONVERTERS.get(targetType);
        if (converter == null) {
            throw new IllegalStateException("No converter found for parameter type: '" + targetType.getSimpleName() + "' (Parameter: '" + paramName + "')");
        }

        try {
            return converter.apply(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to convert request parameter '" + paramName + "' with value '" + value + "' to type '" + targetType.getSimpleName() + "'.", e);
        }
    }

    private static void initializeConverters() {
        CONVERTERS.put(String.class, value -> value);
        CONVERTERS.put(int.class, value -> parseInt(value)); CONVERTERS.put(Integer.class, value -> parseInt(value));
        CONVERTERS.put(long.class, value -> parseLong(value)); CONVERTERS.put(Long.class, value -> parseLong(value));
        CONVERTERS.put(boolean.class, value -> parseBoolean(value)); CONVERTERS.put(Boolean.class, value -> parseBoolean(value));
        CONVERTERS.put(short.class, value -> parseShort(value)); CONVERTERS.put(Short.class, value -> parseShort(value));
        CONVERTERS.put(float.class, value -> parseFloat(value)); CONVERTERS.put(Float.class, value -> parseFloat(value));
        CONVERTERS.put(byte.class, value -> parseByte(value)); CONVERTERS.put(Byte.class, value -> parseByte(value));
        CONVERTERS.put(char.class, value -> parseChar(value)); CONVERTERS.put(Character.class, value -> parseChar(value));
    }

    private static Integer parseInt(String value) {
        return Integer.valueOf(value);
    }

    private static Long parseLong(String value) {
        return Long.valueOf(value);
    }

    private static Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.valueOf(value);
        }
        throw new IllegalArgumentException();
    }

    private static Short parseShort(String value) {
        return Short.valueOf(value);
    }

    private static Float parseFloat(String value) {
        return Float.valueOf(value);
    }

    private static Byte parseByte(String value) {
        return Byte.valueOf(value);
    }

    private static Character parseChar(String value) {
        if (value != null && value.length() == 1) {
            return value.charAt(0);
        }
        throw new IllegalArgumentException();
    }
}
