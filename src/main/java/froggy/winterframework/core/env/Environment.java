package froggy.winterframework.core.env;

import froggy.winterframework.core.PropertySource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 애플리케이션 설정 정보를 로드하고 조회기능을 제공하는 클래스.
 *
 * <p>Environment를 수동 주입받아 런타임에 프로퍼티 값을 조회할 수 있으며,</p>
 * <p>Bean 생성 과정에서 @Value 어노테이션을 감지하여 필드에 주입하는 역할을 함</p>
 */
public class Environment {

    private PropertySource propertySource;

    public Environment() throws IOException {
        this.propertySource = new PropertyLoader().load();
    }

    public PropertySource getPropertySource() {
        return propertySource;
    }

    /**
     * 주어진 Key에 대응하는 값을 반환한다.
     *
     * @param key key
     * @return 설정값
     */
    public String getProperty(String key) {
        return getProperty(key, String.class);
    }


    /**
     * 주어진 Key에 대응하는 값을 반환한다.
     *
     * @param key key
     * @param targetType 반환 타입 클래스
     * @param <T> 반환 타입
     * @return 변환된 설정값
     */
    public <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    /**
     * 주어진 Key에 대응하는 값을 반환한다.
     *
     * @param key key
     * @param defaultValue 조회 실패 시 반환할 기본값
     * @return 설정값
     */
    public String getProperty(String key, String defaultValue) {
        return getProperty(key, String.class, defaultValue);
    }

    /**
     * 주어진 Key에 대응하는 값을 반환한다.
     *
     * @param key key
     * @param targetType 반환 타입 클래스
     * @param defaultValue 조회 실패 시 반환할 기본값
     * @param <T> 반환 타입
     * @return 변환된 설정값
     */
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        String raw = lookupProperty(key);
        if (raw == null) {
            if (defaultValue == null) {
                throw new IllegalStateException(
                    "No property found for key '" + key + "' and no default value provided"
                );
            }
            return defaultValue;
        }
        // 있으면 문자열을 targetType으로 변환
        return convert(raw, targetType);
    }

    private String lookupProperty(String key) {
        String k = (key.startsWith("{$") && key.endsWith("}"))
            ? key.substring(2, key.length()-1)
            : key;
        return propertySource.getSource().get(k);
    }

    private <T> T convert(String raw, Class<T> type) {
        if (type == String.class) {
            return (T) raw;
        }
        if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(raw);
        }
        if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(raw);
        }
        if (type == Double.class || type == double.class) {
            return (T) Double.valueOf(raw);
        }
        if (type == Float.class || type == float.class) {
            return (T) Float.valueOf(raw);
        }
        if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(raw);
        }
        throw new IllegalArgumentException(
            "Invalid conversion: raw value '" + raw + "' cannot be cast to " + type.getSimpleName()
        );
    }

    /**
     * 설정 파일 로드를 담당하는 내부 클래스.
     */
    public class PropertyLoader {
        private PropertySource load() throws IOException {
            Properties prop = new Properties();
            try (InputStream input = ClassLoader.getSystemClassLoader()
                .getResourceAsStream("application.properties")) {
                if (input == null) {
                    throw new FileNotFoundException("application.properties not found in classpath");
                }
                prop.load(input);
            }

            Map<String, String> map = new HashMap<>();
            for (String name : prop.stringPropertyNames()) {
                map.put(name, prop.getProperty(name));
            }

            return new PropertySource("properties", map);
        }
    }
}