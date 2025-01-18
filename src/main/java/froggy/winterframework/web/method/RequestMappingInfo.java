package froggy.winterframework.web.method;

import java.util.Objects;

public class RequestMappingInfo {

    String urlPattern;

    public RequestMappingInfo(String urlPattern) {
        this.urlPattern = urlPattern;
    }

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
