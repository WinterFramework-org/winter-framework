package froggy.winterframework.web.servlet.mvc.method.annotation;

import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.utils.DefaultTypeConverter;
import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.context.request.ServletWebRequest;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.annotation.ModelAndViewMethodReturnValueHandler;
import froggy.winterframework.web.method.annotation.PathVariableMethodArgumentResolver;
import froggy.winterframework.web.method.annotation.RequestBodyMethodArgumentResolver;
import froggy.winterframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import froggy.winterframework.web.method.annotation.RequestParamMethodArgumentResolver;
import froggy.winterframework.web.method.annotation.ResponseBodyMethodReturnValueHandler;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import froggy.winterframework.web.servlet.HandlerAdapter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Controller 애노테이션을 적용한 핸들러를 실행하는 기본 핸들러 어댑터.
 */
public class DefaultControllerHandlerAdapter implements HandlerAdapter {

    private final List<HandlerMethodArgumentResolver> resolvers = new LinkedList<>();
    private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new LinkedList<>();

    public DefaultControllerHandlerAdapter() {
        initResolver();
        initReturnValueHandlers();
    }

    private void initResolver() {
        TypeConverter converter = new DefaultTypeConverter();

        resolvers.add(new RequestParamMethodArgumentResolver(converter));
        resolvers.add(new PathVariableMethodArgumentResolver(converter));
        resolvers.add(new RequestBodyMethodArgumentResolver());
        resolvers.add(new RequestHeaderMethodArgumentResolver(converter));
    }

    private void initReturnValueHandlers() {
        returnValueHandlers.add(new ModelAndViewMethodReturnValueHandler());
        returnValueHandlers.add(new ResponseBodyMethodReturnValueHandler());
    }

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> customArgumentResolvers) {
        resolvers.addAll(customArgumentResolvers);
    }

    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> customReturnValueHandlers) {
        returnValueHandlers.addAll(customReturnValueHandlers);
    }

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

        NativeWebRequest webRequest = new ServletWebRequest(request, response);

        Object[] args = getMethodArgumentValues(webRequest, method.getParameters());

        Object returnValue = invokeHandlerMethod(instance, method, args);

        for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {
            if (returnValueHandler.supportsReturnType(handlerMethod)) {
                returnValueHandler.handleReturnValue(returnValue, returnValue.getClass(), webRequest);
                return ModelAndView.createModelAndView(returnValue);
            }
        }

        throw new IllegalStateException("No suitable HandlerMethodReturnValueHandler found for return type: "
            + returnValue.getClass().getName() + " in method: "
            + instance.getClass().getSimpleName() + "#" + method.getName());
    }

    /**
     * HTTP Request에서 값을 추출하여 Handler(Controller)의 호출에 필요한 인자 값을 생성
     *
     * @param webRequest 현재 Request 컨텍스트
     * @param parameters 메소드의 파라미터 배열
     * @return 생성된 인자 값 배열
     * @throws Exception 인자 값을 해결할 수 없는 경우 예외 발생
     */
    public Object[] getMethodArgumentValues(NativeWebRequest webRequest, Parameter[] parameters)
        throws Exception {
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            args[i] = resolveArgumentForParameter(parameters[i], webRequest);
        }

        return args;
    }

    /**
     * 특정 파라미터의 값을 변환하여 반환
     *
     * <p>등록된 {@link HandlerMethodArgumentResolver} 목록을 순회하며
     * 해당 파라미터를 처리할 수 있는 Resolver를 찾아 변환.</p>
     *
     * @param parameter 변환할 파라미터
     * @param webRequest 현재 Request 컨텍스트
     * @return 변환된 파라미터 값, 해결할 수 없는 경우 {@code null} 반환
     * @throws Exception 변환 과정에서 예외 발생 시
     */
    private Object resolveArgumentForParameter(Parameter parameter, NativeWebRequest webRequest)
        throws Exception {
        for (HandlerMethodArgumentResolver resolver : resolvers) {
            if (resolver.supportsParameter(parameter)) {
                return resolver.resolveArgument(parameter, webRequest);
            }
        }
        return null;
    }

    private Object invokeHandlerMethod(Object instance, Method method, Object[] args) throws Exception {
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to invoke handler method: " + instance.getClass() + "#" + method.getName(), e);
        } catch (InvocationTargetException e) {
            Throwable ex = e.getCause();
            if (ex instanceof Exception) {
                throw (Exception) ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
}
