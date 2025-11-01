package froggy.winterframework.web.context.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * {@link HttpServletRequest}와 {@link HttpServletResponse}를 다루기 위한
 * Servlet 기반 {@link NativeWebRequest} 구현체.
 */
public class ServletWebRequest implements NativeWebRequest {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public ServletWebRequest(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 지정된 ClassType으로 원본 Request 반환
     */
    @Override
    public <T> T getNativeRequest(Class<T> requiredType) {
        if (requiredType != null && requiredType.isAssignableFrom(this.request.getClass())) {
            return requiredType.cast(request);
        }
        return null;
    }

    /**
     * 지정된 ClassType으로 원본 Reponse 반환
     */
    @Override
    public <T> T getNativeResponse(Class<T> requiredType) {
        if (requiredType != null && requiredType.isAssignableFrom(this.response.getClass())) {
            return requiredType.cast(response);
        }
        return null;
    }

    @Override
    public String getHeader(String headerName) {
        return this.request.getHeader(headerName);
    }

    @Override
    public String getParameter(String paramName) {
        return this.request.getParameter(paramName);
    }

    @Override
    public Object getAttribute(String name, int scope) {
        if (SCOPE_REQUEST == scope) {
            return this.request.getAttribute(name);
        }
        else if (SCOPE_SESSION == scope) {
            HttpSession session = this.request.getSession(false);
            return session != null ? session.getAttribute(name) : null;
        }

        return null;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        if (SCOPE_REQUEST == scope) {
            this.request.setAttribute(name, value);
        }
        else if (SCOPE_SESSION == scope) {
            HttpSession session = this.request.getSession(true);
            session.setAttribute(name, value);
        }
    }
}
