package froggy.winterframework.web.method;

import froggy.winterframework.web.bind.annotation.HttpMethod;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 요청 URL 패턴과 매핑된 Handler(Controller) 메소드 정보를 비교하는 클래스.
 *
 * <p>DispatcherServlet이 요청을 처리할 때,
 * 등록된 {@link #urlPattern}와 요청 URL과 HTTP Method를 비교하여 적절한 메소드를 실행하도록 지원.
 */
public class RequestMappingInfo {

    private String urlPattern;
    private Set<HttpMethod> httpMethods;

    public RequestMappingInfo(String urlPattern, HttpMethod... httpMethods) {
        this(urlPattern, new LinkedHashSet<>(Arrays.asList(httpMethods)));
    }

    public RequestMappingInfo(String urlPattern, Set<HttpMethod> httpMethods) {
        this.urlPattern = initUrlPattern(urlPattern);
        this.httpMethods = httpMethods;
    }

    public static RequestMappingInfo emptyRequestMappingInfo() {
        return new RequestMappingInfo("", Collections.emptySet());
    }

    /**
     * URL 패턴 초기화.
     *
     * <p>불필요한 '/'가 없도록 조정하여 반환</p>
     *
     * @param urlPattern 초기화할 URL 패턴
     * @return 정규화된 URL 패턴
     */
    private String initUrlPattern(String urlPattern) {
        if (urlPattern == null || urlPattern.isEmpty()) {
            return "";
        }

        // URL 앞 "/" 추가
        urlPattern = urlPattern.startsWith("/") ? urlPattern : "/" + urlPattern;

        // URL 마지막 "/" 제거
        return urlPattern.endsWith("/") ? urlPattern.substring(0, urlPattern.length() - 1) : urlPattern;
    }

    /**
     * 현재 RequestMappingInfo와 다른 RequestMappingInfo를 결합.
     *
     * @param other 결합할 다른 RequestMappingInfo
     * @return 결합된 RequestMappingInfo
     */
    public RequestMappingInfo combine(RequestMappingInfo other) {
        return new RequestMappingInfo(
            combineUrl(other),
            combineHttpMethods(other)
        );
    }

    private String combineUrl(RequestMappingInfo other) {
        return this.urlPattern + other.urlPattern;
    }

    private Set<HttpMethod> combineHttpMethods(RequestMappingInfo other) {
        // 클래스 레벨과 메서드레벨 모두 HttpMethod가 명시되지 않은 경우 모든 메서드 허용
        if (this.httpMethods.isEmpty() && other.httpMethods.isEmpty()) {
            return new LinkedHashSet<>(Arrays.asList(HttpMethod.values()));
        }

        Set<HttpMethod> combined = new LinkedHashSet<>(this.httpMethods);
        combined.addAll(other.httpMethods);
        return combined;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public Set<HttpMethod> getHttpMethods() {
        return httpMethods;
    }

    /**
     * 두 개의 RequestMappingInfo 객체가 같은 URL 패턴, HTTP Method를 비교.
     *
     * @param o 비교 대상 객체
     * @return urlPattern이 같고 HTTP Method를 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestMappingInfo other = (RequestMappingInfo) o;
        if (Collections.disjoint(this.httpMethods, other.httpMethods)) {
            return false;
        }

        return urlPattern.equals(((RequestMappingInfo) o).urlPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlPattern);
    }

}
