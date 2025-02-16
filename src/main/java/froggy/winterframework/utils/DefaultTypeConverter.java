package froggy.winterframework.utils;

import froggy.winterframework.utils.convert.TypeConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 특정 타입으로 변환하는 Converter
 */
public class DefaultTypeConverter implements TypeConverter {

    // 특정 타입으로의 변환 메소드를 담고있는 Map
    private final Map<Class<?>, Function<String, ?>> CONVERTERS = new HashMap<>();

    public DefaultTypeConverter() {
        initializeConverters();
    }

    private void initializeConverters() {
        CONVERTERS.put(String.class, value -> value);
        CONVERTERS.put(int.class, value -> parseInt(value)); CONVERTERS.put(Integer.class, value -> parseInt(value));
        CONVERTERS.put(long.class, value -> parseLong(value)); CONVERTERS.put(Long.class, value -> parseLong(value));
        CONVERTERS.put(boolean.class, value -> parseBoolean(value)); CONVERTERS.put(Boolean.class, value -> parseBoolean(value));
        CONVERTERS.put(short.class, value -> parseShort(value)); CONVERTERS.put(Short.class, value -> parseShort(value));
        CONVERTERS.put(float.class, value -> parseFloat(value)); CONVERTERS.put(Float.class, value -> parseFloat(value));
        CONVERTERS.put(byte.class, value -> parseByte(value)); CONVERTERS.put(Byte.class, value -> parseByte(value));
        CONVERTERS.put(char.class, value -> parseChar(value)); CONVERTERS.put(Character.class, value -> parseChar(value));
    }

    /**
     * 해당 타입을 변환할 수 있는지 확인.
     * @param clazz 변환하려는 클래스 타입
     * @return 변환 가능하면 {@code true}, 그렇지 않으면 {@code false}
     */
    public Boolean isSupport(Class<?> clazz) {
        return CONVERTERS.containsKey(clazz);
    }

    /**
     * 주어진 값을 특정 타입으로 변환하는 메서드
     * @param targetType 변환하려는 대상 타입
     * @param value 변환할 값 (문자열)
     * @param <T> 변환된 타입 반환
     * @return 변환된 값
     * @throws IllegalArgumentException 변환할 수 없는 경우 예외 발생
     */
    public <T> T convert(Class<T> targetType, Object value) {
        Function<String, ?> converter = CONVERTERS.get(targetType);

        if (converter == null) {
            throw new IllegalArgumentException("No converter found for type: " + targetType.getSimpleName());
        }

        try {
            return (T) converter.apply((String) value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Can't convert value '" + value + "' to type '" + targetType.getSimpleName() + "'.", e);
        }
    }

    private Integer parseInt(String value) {
        return Integer.valueOf(value);
    }

    private Long parseLong(String value) {
        return Long.valueOf(value);
    }

    private Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.valueOf(value);
        }
        throw new IllegalArgumentException();
    }

    private Short parseShort(String value) {
        return Short.valueOf(value);
    }

    private Float parseFloat(String value) {
        return Float.valueOf(value);
    }

    private Byte parseByte(String value) {
        return Byte.valueOf(value);
    }

    private Character parseChar(String value) {
        if (value != null && value.length() == 1) {
            return value.charAt(0);
        }
        throw new IllegalArgumentException();
    }

}
