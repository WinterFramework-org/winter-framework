package froggy.winterframework.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP Header를 표현하는 클래스.
 *
 * <p>하나의 Header 이름에 여러 값을 저장할 수 있도록
 * {@code Map<String, List<String>>} 형태로 구성한다.
 * Header 이름은 대소문자를 구분하지 않으며,
 * 내부적으로 소문자로 정규화해 처리한다.
 */
public class HttpHeaders {

    public static final String CONTENT_TYPE = "content-type";
    public static final String LOCATION = "location";

    private final Map<String, List<String>> headers = new LinkedHashMap<>();

    public HttpHeaders() {
    }

    /**
     * 기존 Header 값을 복사하여 새로운 Header 인스턴스를 생성한다.
     * 원본과 분리된 Header 값을 구성할 때 사용한다.
     */
    public HttpHeaders(HttpHeaders other) {
        if (other != null) {
            putAll(other);
        }
    }

    /**
     * 지정된 이름의 Header에 값을 추가한다.
     * <p>이미 동일한 이름의 Header가 존재할 경우, 기존 값을 유지한 채 새로운 값을 목록에 누적한다.
     */
    void add(String name, String value) {
        String normalizedName = normalize(name);
        List<String> values = this.headers.get(normalizedName);
        if (values == null) {
            values = new ArrayList<>();
            this.headers.put(normalizedName, values);
        }
        values.add(value);
    }

    /**
     * 지정된 이름의 Header 값을 설정한다,
     * <p>같은 이름의 Header가 있으면 기존 값을 교체한다.
     */
    void set(String name, String value) {
        String normalizedName = normalize(name);
        List<String> values = new ArrayList<>();
        values.add(value);
        this.headers.put(normalizedName, values);
    }

    /**
     * 지정된 이름의 모든 Header 값들을 읽기 전용 목록으로 반환한다.
     * 값이 없을 경우 빈 목록을 반환하여 한다,
     */
    public List<String> get(String name) {
        List<String> values = this.headers.get(normalize(name));
        if (values == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * 지정한 이름의 첫 번째 Header 값을 반환한다.
     * 값이 없으면 {@code null}을 반환한다.
     */
    public String getFirst(String name) {
        List<String> values = get(name);
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    /**
     * 다른 HttpHeaders 객체의 모든 값을 현재 객체로 병합한다.
     */
    void putAll(HttpHeaders other) {
        if (other == null) {
            return;
        }

        for (Map.Entry<String, List<String>> entry : other.headers.entrySet()) {
            for (String value : entry.getValue()) {
                add(entry.getKey(), value);
            }
        }
    }

    /**
     * Header가 비어있는지 여부를 반환한다.
     */
    public boolean isEmpty() {
        return this.headers.isEmpty();
    }

    /**
     * 현재 Header 값을 읽기 전용 Map 복사본으로 반환한다.
     * 반환한 Map과 List는 수정할 수 없다.
     */
    public Map<String, List<String>> toMap() {
        Map<String, List<String>> copied = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : this.headers.entrySet()) {
            copied.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copied);
    }

    /**
     * Header 이름을 소문자로 정규화한다.
     */
    private String normalize(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        return name.toLowerCase(Locale.ROOT);
    }
}
