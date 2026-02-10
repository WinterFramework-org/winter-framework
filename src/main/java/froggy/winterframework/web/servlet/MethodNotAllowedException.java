package froggy.winterframework.web.servlet;

import java.util.Set;
import javax.servlet.ServletException;

/**
 * URL은 존재하지만 HTTP Method가 허용되지 않을 때 발생하는 예외 (HTTP 405).
 */
public class MethodNotAllowedException extends ServletException {

    private final String httpMethod;
    private final String requestURL;
    private final Set<String> allowedMethods;

    public MethodNotAllowedException(String httpMethod, String requestURL, Set<String> allowedMethods) {
        super("Request method '" + httpMethod + "' not supported for " + requestURL);
        this.httpMethod = httpMethod;
        this.requestURL = requestURL;
        this.allowedMethods = allowedMethods;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public Set<String> getAllowedMethods() {
        return allowedMethods;
    }

    public String getAllowHeader() {
        return String.join(", ", allowedMethods);
    }
}
