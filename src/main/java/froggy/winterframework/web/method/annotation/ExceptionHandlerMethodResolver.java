package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.WinterUtils;
import froggy.winterframework.web.bind.annotation.ExceptionHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 컨트롤러 클래스에서 예외 처리 메서드를 찾아 예외 타입과 연결한다.
 *
 * <p>이 클래스는 컨트롤러의 {@link ExceptionHandler} 메서드를 한 번만 분석하고,
 * 이후에는 발생한 예외 타입에 맞는 처리 메서드를 빠르게 찾는 용도로 사용한다.
 */
public class ExceptionHandlerMethodResolver {

    private final Map<Class<? extends Throwable>, Method> mappedMethods = new LinkedHashMap<>();

    public ExceptionHandlerMethodResolver(Class<?> handlerType) {
        for (Method method : handlerType.getMethods()) {
            if (!WinterUtils.hasAnnotation(method, ExceptionHandler.class)) {
                continue;
            }
            registerExceptionMappings(method);
        }
    }

    /**
     * 등록된 예외 처리 메서드가 있는지 확인한다.
     *
     * @return 예외 처리 메서드가 하나 이상 있으면 {@code true}
     */
    public boolean hasExceptionMappings() {
        return !mappedMethods.isEmpty();
    }

    /**
     * 발생한 예외에 맞는 처리 메서드를 찾는다.
     *
     * <p>현재 예외 타입으로 먼저 찾고, 없으면 원인 예외를 따라가며 다시 찾는다.
     *
     * @param exception 발생한 예외
     * @return 처리 메서드, 없으면 {@code null}
     */
    public Method resolveMethod(Exception exception) {
        Method method = resolveMethodByExceptionType(exception.getClass());
        if (method != null) {
            return method;
        }

        // 현재 예외에서 찾지 못하면 원인 예외를 따라가며 찾는다.
        Throwable cause = exception.getCause();
        while (cause != null) {
            method = resolveMethodByExceptionType(cause.getClass());
            if (method != null) {
                return method;
            }
            if (cause == cause.getCause()) {
                break;
            }
            cause = cause.getCause();
        }

        return null;
    }

    /**
     * 예외 타입에 맞는 처리 메서드를 찾는다.
     *
     * @param exceptionType 발생한 예외 타입
     * @return 처리 메서드, 없으면 {@code null}
     */
    public Method resolveMethodByExceptionType(Class<? extends Throwable> exceptionType) {
        return getMappedMethod(exceptionType);
    }

    /**
     * 메서드에 선언된 처리 대상 예외를 읽어 매핑한다.
     *
     * @param method 예외 처리 메서드
     */
    private void registerExceptionMappings(Method method) {
        List<Class<? extends Throwable>> exceptionTypes = resolveExceptionTypes(method);
        if (exceptionTypes.isEmpty()) {
            throw new IllegalStateException("No exception types mapped to " + method);
        }

        for (Class<? extends Throwable> exceptionType : exceptionTypes) {
            addExceptionMapping(exceptionType, method);
        }
    }

    /**
     * 메서드가 처리할 예외 타입 목록을 만든다.
     *
     * <p>어노테이션에 예외 타입이 없으면 메서드 파라미터의 예외 타입을 사용한다.
     *
     * @param method 예외 처리 메서드
     * @return 처리 대상 예외 타입 목록
     */
    @SuppressWarnings("unchecked")
    private List<Class<? extends Throwable>> resolveExceptionTypes(Method method) {
        List<Class<? extends Throwable>> exceptionTypes = new ArrayList<>();
        ExceptionHandler annotation = method.getAnnotation(ExceptionHandler.class);

        // @ExceptionHandler에 선언한 예외 타입을 사용한다.
        if (annotation != null && annotation.value().length > 0) {
            exceptionTypes.addAll(Arrays.asList(annotation.value()));
            return exceptionTypes;
        }

        for (Class<?> parameterType : method.getParameterTypes()) {
            if (Throwable.class.isAssignableFrom(parameterType)) {
                exceptionTypes.add((Class<? extends Throwable>) parameterType);
            }
        }

        return exceptionTypes;
    }

    /**
     * 예외 타입과 처리 메서드를 연결한다.
     *
     * @param exceptionType 처리 대상 예외 타입
     * @param method 예외 처리 메서드
     */
    private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {
        Method oldMethod = mappedMethods.put(exceptionType, method);
        // 같은 예외 타입의 중복 매핑은 허용하지 않는다.
        if (oldMethod != null && !oldMethod.equals(method)) {
            throw new IllegalStateException(
                "Ambiguous @ExceptionHandler method mapped for [" + exceptionType.getName() + "]: {"
                    + oldMethod + ", " + method + "}"
            );
        }
    }

    /**
     * 현재 예외 타입에 가장 가까운 처리 메서드를 찾는다.
     *
     * @param exceptionType 발생한 예외 타입
     * @return 가장 가까운 처리 메서드, 없으면 {@code null}
     */
    private Method getMappedMethod(Class<? extends Throwable> exceptionType) {
        Method matchedMethod = null;
        int bestMatchDepth = Integer.MAX_VALUE;

        for (Map.Entry<Class<? extends Throwable>, Method> entry : mappedMethods.entrySet()) {
            Class<? extends Throwable> mappedExceptionType = entry.getKey();
            if (!mappedExceptionType.isAssignableFrom(exceptionType)) {
                continue;
            }

            int currentDepth = calculateDepth(exceptionType, mappedExceptionType);
            // 현재 예외 타입과 가장 가까운 매핑을 선택한다.
            if (currentDepth < bestMatchDepth) {
                matchedMethod = entry.getValue();
                bestMatchDepth = currentDepth;
            }
        }

        return matchedMethod;
    }

    /**
     * 발생한 예외와 처리 대상 예외 사이의 거리를 계산한다.
     *
     * <p>값이 작을수록 더 구체적인 예외 처리 메서드다.
     *
     * @param exceptionType 발생한 예외 타입
     * @param mappedExceptionType 처리 대상 예외 타입
     * @return 두 타입 사이의 상속 거리
     */
    private int calculateDepth(
        Class<? extends Throwable> exceptionType,
        Class<? extends Throwable> mappedExceptionType
    ) {
        int depth = 0;
        Class<?> current = exceptionType;

        while (current != null) {
            if (current.equals(mappedExceptionType)) {
                return depth;
            }
            current = current.getSuperclass();
            depth++;
        }

        return Integer.MAX_VALUE;
    }
}
