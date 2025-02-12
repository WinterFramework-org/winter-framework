package froggy.winterframework.web.servlet.mvc.method.annotation;

import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.servlet.HandlerAdapter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Controller 애노테이션을 적용한 핸들러를 실행하는 기본 핸들러 어댑터.
 */
public class DefaultControllerHandlerAdapter implements HandlerAdapter {

    /**
     * 해당 핸들러가 @Controller 여부를 확인하여 지원 여부 결정.
     *
     * @param handler 검사할 핸들러 객체
     * @return 지원 한다면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supports(Object handler) {
        return ((HandlerMethod) handler)
            .getHandlerInstance()
            .getClass()
            .isAnnotationPresent(Controller.class);
    }

    /**
     * 요청을 처리하고 응답을 생성
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler  요청을 처리할 핸들러 객체
     * @return 처리 결과를 포함한 ModelAndView
     * @throws Exception 핸들러 실행 중 예외
     */
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
