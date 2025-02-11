package froggy.winterframework.web.servlet.mvc.method.annotation;

import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.servlet.HandlerAdapter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultControllerHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return ((HandlerMethod) handler)
            .getHandlerInstance()
            .getClass()
            .isAnnotationPresent(Controller.class);
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Object instance = handlerMethod.getHandlerInstance();

        try {
            return (ModelAndView) method.invoke(instance, request, response);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to invoke handler method: " + instance.getClass() + "#" + method.getName(), e);
        }
    }
}
