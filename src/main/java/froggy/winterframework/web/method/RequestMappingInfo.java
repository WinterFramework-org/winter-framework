package froggy.winterframework.web.method;

import java.util.Objects;

/**
 * 요청 URL 패턴과 매핑된 Handler(Controller) 메소드 정보를 비교하는 클래스.
 *
 * <p>DispatcherServlet이 요청을 처리할 때,
 * 등록된 {@link #urlPattern}와 요청 URL을 비교하여 적절한 메소드를 실행하도록 지원.
 */
public class RequestMappingInfo {

    String urlPattern;

    public RequestMappingInfo(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    /**
     * 두 개의 RequestMappingInfo 객체가 같은 URL 패턴을 가지는지 비교.
     *
     * @param o 비교 대상 객체
     * @return urlPattern이 같으면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMappingInfo requestMappingInfo = (RequestMappingInfo) o;
        return Objects.equals(urlPattern, requestMappingInfo.urlPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlPattern);
    }
}
