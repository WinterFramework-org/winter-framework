package froggy.winterframework.web.method.support;

import froggy.winterframework.web.method.HandlerMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerMethodReturnValueHandler {

    boolean supportsReturnType(HandlerMethod handlerMethod);

    void handleReturnValue(Object returnValue, Class<?> returnType, HttpServletRequest request, HttpServletResponse response);
}