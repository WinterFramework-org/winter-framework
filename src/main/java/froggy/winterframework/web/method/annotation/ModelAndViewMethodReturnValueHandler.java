package froggy.winterframework.web.method.annotation;

import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ModelAndViewMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(HandlerMethod handlerMethod) {
        return handlerMethod.getReturnType().isAssignableFrom(ModelAndView.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, Class<?> returnType,
        HttpServletRequest request, HttpServletResponse response) {

    }
}
