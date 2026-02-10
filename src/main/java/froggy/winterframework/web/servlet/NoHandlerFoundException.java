package froggy.winterframework.web.servlet;

import javax.servlet.ServletException;

/**
 * 요청 URI에 매핑된 핸들러가 없을 때 발생하는 예외 (HTTP 404).
 */
public class NoHandlerFoundException extends ServletException {

    private final String httpMethod;
    private final String requestURL;

    public NoHandlerFoundException(String httpMethod, String requestURL) {
        super("No handler found for " + httpMethod + " " + requestURL);
        this.httpMethod = httpMethod;
        this.requestURL = requestURL;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }
}
