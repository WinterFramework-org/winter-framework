package froggy.winterframework.web.servlet.mvc.method.annotation;

import froggy.winterframework.core.MethodParameter;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.context.request.ServletWebRequest;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.annotation.ExceptionHandlerMethodResolver;
import froggy.winterframework.web.method.annotation.ModelAndViewMethodReturnValueHandler;
import froggy.winterframework.web.method.annotation.ResponseBodyMethodReturnValueHandler;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import froggy.winterframework.web.servlet.ExceptionResolver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@code @ExceptionHandler} 애노테이션이 선언된 메서드를 찾아 예외를 처리하는 리졸버 클래스.
 *
 * <p>예외가 발생한 컨트롤러 내부의 처리 메서드를 선택하고 실행하여 최종 응답을 반환한다.
 */
public class ExceptionHandlerExceptionResolver implements ExceptionResolver {

    private final ConcurrentHashMap<Class<?>, ExceptionHandlerMethodResolver> exceptionHandlerCache =
        new ConcurrentHashMap<Class<?>, ExceptionHandlerMethodResolver>();

    private final List<HandlerMethodArgumentResolver> argumentResolvers
        = new ArrayList<HandlerMethodArgumentResolver>();

    private final List<HandlerMethodReturnValueHandler> returnValueHandlers =
        new ArrayList<HandlerMethodReturnValueHandler>();

    public ExceptionHandlerExceptionResolver() {
        initReturnValueHandlers();
    }

    private void initReturnValueHandlers() {
        returnValueHandlers.add(new ModelAndViewMethodReturnValueHandler());
        returnValueHandlers.add(new ResponseBodyMethodReturnValueHandler());
    }

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            return;
        }

        this.argumentResolvers.addAll(argumentResolvers);
    }

    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers == null || returnValueHandlers.isEmpty()) {
            return;
        }

        this.returnValueHandlers.addAll(returnValueHandlers);
    }

    /**
     * 현재 컨트롤러에서 예외를 처리할 {@code @ExceptionHandler} 메서드를 찾아 실행한다.
     *
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param handler 예외가 발생한 원래 핸들러
     * @param exception 발생한 예외
     * @return 예외 처리 결과, 처리 메서드가 없으면 {@code null}
     */
    @Override
    public ModelAndView resolveException(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception exception
    ) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method exceptionHandlerMethod = getExceptionHandlerMethod(handlerMethod, exception);
        if (exceptionHandlerMethod == null) {
            return null;
        }

        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        ModelAndView mavContainer = ModelAndView.createContainer();

        try {
            Object[] args = getMethodArgumentValues(
                exceptionHandlerMethod, exception, handlerMethod, webRequest, mavContainer);
            Object returnValue = invokeExceptionHandlerMethod(
                handlerMethod.getHandlerInstance(), exceptionHandlerMethod, args);
            return handleReturnValue(
                handlerMethod, exceptionHandlerMethod, returnValue, webRequest, mavContainer);
        } catch (Exception invocationException) {
            System.err.println("Failure in @ExceptionHandler " + methodSignature(exceptionHandlerMethod)
                + " - " + invocationException.getMessage());

            // 예외 처리 메서드 호출이 실패하면 다음 resolver로 넘긴다.
            return null;
        }
    }

    /**
     * 현재 컨트롤러에서 예외를 처리할 메서드를 찾는다.
     *
     * @param handlerMethod 예외가 발생한 원래 핸들러 메서드
     * @param exception 발생한 예외
     * @return 처리 메서드, 없으면 {@code null}
     */
    private Method getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
        Class<?> handlerType = handlerMethod.getHandlerType();
        // handler 타입별 @ExceptionHandler 매핑을 캐시한다.
        ExceptionHandlerMethodResolver resolver = exceptionHandlerCache.get(handlerType);
        if (resolver == null) {
            resolver = new ExceptionHandlerMethodResolver(handlerType);
            exceptionHandlerCache.put(handlerType, resolver);
        }

        if (!resolver.hasExceptionMappings()) {
            return null;
        }

        return resolver.resolveMethod(exception);
    }

    /**
     * 예외 처리 메서드 호출에 필요한 인자를 만든다.
     *
     * @param method 예외 처리 메서드
     * @param exception 발생한 예외
     * @param handlerMethod 예외가 발생한 원래 핸들러 메서드
     * @param webRequest 현재 요청 컨텍스트
     * @param mavContainer 예외 처리 결과 컨테이너
     * @return 호출 인자 배열
     */
    private Object[] getMethodArgumentValues(
        Method method,
        Exception exception,
        HandlerMethod handlerMethod,
        NativeWebRequest webRequest,
        ModelAndView mavContainer
    ) {
        MethodParameter[] parameters = MethodParameter.forMethod(method);
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            args[i] = resolveArgument(parameters[i], exception, handlerMethod, webRequest, mavContainer);
        }

        return args;
    }

    /**
     * 예외 처리 메서드의 파라미터 하나를 해석한다.
     *
     * @param parameter 대상 파라미터
     * @param exception 발생한 예외
     * @param handlerMethod 예외가 발생한 원래 핸들러 메서드
     * @param webRequest 현재 요청 컨텍스트
     * @param mavContainer 예외 처리 결과 컨테이너
     * @return 파라미터 값
     */
    private Object resolveArgument(
        MethodParameter parameter,
        Exception exception,
        HandlerMethod handlerMethod,
        NativeWebRequest webRequest,
        ModelAndView mavContainer
    ) {
        Class<?> parameterType = parameter.getParameterType();

        if (parameterType.isAssignableFrom(exception.getClass())) {
            return exception;
        }

        Throwable cause = exception.getCause();
        while (cause != null) {
            if (parameterType.isAssignableFrom(cause.getClass())) {
                return cause;
            }
            if (cause == cause.getCause()) {
                break;
            }
            cause = cause.getCause();
        }

        if (HttpServletRequest.class.isAssignableFrom(parameterType)) {
            return webRequest.getNativeRequest(HttpServletRequest.class);
        }

        if (HttpServletResponse.class.isAssignableFrom(parameterType)) {
            // 응답 객체 주입 시 직접 응답 처리로 표시한다.
            mavContainer.setRequestHandled(true);
            return webRequest.getNativeResponse(HttpServletResponse.class);
        }

        if (NativeWebRequest.class.isAssignableFrom(parameterType)) {
            return webRequest;
        }

        if (HandlerMethod.class.isAssignableFrom(parameterType)) {
            return handlerMethod;
        }

        for (HandlerMethodArgumentResolver resolver : argumentResolvers) {
            if (resolver.supportsParameter(parameter)) {
                try {
                    return resolver.resolveArgument(parameter, webRequest, mavContainer);
                } catch (Exception ex) {
                    throw new IllegalStateException(
                        "Failed to resolve argument of type [" + parameter.getParameterType().getName() + "] "
                            + "in @ExceptionHandler method: " + methodSignature(parameter.getMethod()), ex);
                }
            }
        }

        throw new IllegalStateException(
            "Unsupported @ExceptionHandler parameter type: " + parameterType.getName()
                + " in method: " + methodSignature(parameter.getMethod())
        );
    }

    /**
     * 예외 처리 메서드를 실제로 호출한다.
     *
     * @param handler 예외 처리 메서드를 가진 컨트롤러 인스턴스
     * @param method 예외 처리 메서드
     * @param args 호출 인자
     * @return 메서드 반환값
     * @throws Exception 메서드 실행 중 발생한 예외
     */
    private Object invokeExceptionHandlerMethod(Object handler, Method method, Object[] args)
        throws Exception {
        try {
            return method.invoke(handler, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Failed to access @ExceptionHandler method: " + methodSignature(method), e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getCause();
            if (targetException instanceof Exception) {
                throw (Exception) targetException;
            }
            throw new RuntimeException(targetException);
        }
    }

    /**
     * 예외 처리 메서드의 반환값을 응답 결과로 바꾼다.
     *
     * @param originalHandlerMethod 예외가 발생한 원래 핸들러 메서드
     * @param exceptionHandlerMethod 예외 처리 메서드
     * @param returnValue 예외 처리 메서드 반환값
     * @param webRequest 현재 요청 컨텍스트
     * @param mavContainer 예외 처리 결과 컨테이너
     * @return 최종 ModelAndView
     */
    private ModelAndView handleReturnValue(
        HandlerMethod originalHandlerMethod,
        Method exceptionHandlerMethod,
        Object returnValue,
        NativeWebRequest webRequest,
        ModelAndView mavContainer
    ) {
        HandlerMethod exceptionHandler = new HandlerMethod(
            originalHandlerMethod.getHandlerInstance(),
            originalHandlerMethod.getHandlerType(),
            exceptionHandlerMethod
        );

        if (returnValue == null) {
            if (void.class.equals(exceptionHandlerMethod.getReturnType())
                || webRequest.getNativeResponse(HttpServletResponse.class).isCommitted()
                || mavContainer.isRequestHandled()) {
                mavContainer.setRequestHandled(true);
                return mavContainer;
            }

            throw new IllegalStateException(
                "@ExceptionHandler must not return null without handling the response: "
                    + methodSignature(exceptionHandlerMethod)
            );
        }

        // 반환값이 있으면 최종 응답 처리 여부는 ReturnValueHandler가 다시 결정한다.
        mavContainer.setRequestHandled(false);

        for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {
            if (returnValueHandler.supportsReturnType(exceptionHandler)) {
                returnValueHandler.handleReturnValue(
                    returnValue, returnValue.getClass(), webRequest, mavContainer);
                return mavContainer;
            }
        }

        if (returnValue instanceof String) {
            mavContainer.setView((String) returnValue);
            return mavContainer;
        }

        throw new IllegalStateException(
            "No suitable return value handler for @ExceptionHandler method: "
                + methodSignature(exceptionHandlerMethod)
        );
    }

    private String methodSignature(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }
}
