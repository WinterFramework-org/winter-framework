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

        this.urlPattern = urlPattern;
        this.httpMethods = new LinkedHashSet<>(Arrays.asList(httpMethods));
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

        if (!matchUrlPattern(this.urlPattern, ((RequestMappingInfo) o).getUrlPattern())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlPattern);
    }

    private boolean matchUrlPattern(String pattern, String url) {
        String[] patternParts = pattern.split("/");
        String[] urlParts = url.split("/");

        // @PathVariable로 등록되어있는 UrlPattern인지 확인
        if (! pattern.contains("*")) {
            return pattern.equals(url);
        }

        if (patternParts.length != urlParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            if (!patternParts[i].equals("/*") && !patternParts[i].equals(urlParts[i])) {
                return false;
            }
        }

        return true;
    }

}
