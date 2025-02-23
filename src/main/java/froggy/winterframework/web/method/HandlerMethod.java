package froggy.winterframework.web.method;

import froggy.winterframework.web.bind.annotation.PathVariable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Handler(Controller)의 특정 메소드 정보를 저장하고 실행하는 클래스.
 *
 * <p>요청이 들어오면 매핑된 메소드 정보를 저장하고,
 * 메소드를 실행할 수 있도록 지원
 */
public class HandlerMethod {

    Object handlerInstance;
    Class<?> handlerType;
    private final Method method;
    private final Parameter[] parameters;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private final boolean hasPathVariable;

    private HandlerMethod(Object handlerInstance, Class<?> handlerType, Method method, Parameter[] parameters,
        Class<?>[] parameterTypes, Class<?> returnType) {
        this.handlerInstance = handlerInstance;
        this.handlerType = handlerType;
        this.method = method;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.hasPathVariable = hasPathVariable();
    }

    public HandlerMethod(Object handlerInstance, Class<?> handlerType, Method method) {
        this(
            handlerInstance,
            handlerType,
            method,
            method.getParameters(),
            method.getParameterTypes(),
            method.getReturnType()
        );
    }


    /**
     * Handler(Controller) 인스턴스를 반환.
     *
     * @return Handler 인스턴스
     */
    public Object getHandlerInstance() {
        return handlerInstance;
    }

    /**
     * Handler(Controller) Type을 반환.
     *
     * @return Handler Class Type
     */
    public Class<?> getHandlerType() {
        return handlerType;
    }

    /**
     * 매핑된 메소드를 반환.
     *
     * @return 매핑된 메소드 {@link Method}
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 메소드의 매개변수 목록을 반환.
     *
     * @return 매개변수 배열
     */
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * 메소드의 매개변수 타입 배열을 반환.
     *
     * @return 매개변수 타입 배열
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * 메소드의 반환 타입을 반환.
     *
     * @return 반환 타입
     */
    public Class<?> getReturnType() {
        return returnType;
    }


    /**
     * 현재 파라미터 목록에 `@PathVariable`이 포함되어 있는지 여부 반환
     *
     * @return {@code true}이면 `@PathVariable`이 존재, 그렇지 않으면 {@code false}
     */
    public boolean isHasPathVariable() {
        return hasPathVariable;
    }


    /**
     * 파라미터 목록에서 `@PathVariable` 애노테이션이 존재하는지 확인
     *
     * @return {@code true}이면 `@PathVariable`이 존재, 그렇지 않으면 {@code false}
     */
    private boolean hasPathVariable() {
        for (Parameter p : this.parameters) {
            if (p.isAnnotationPresent(PathVariable.class)) {
                return true;
            }
        }
        return false;
    }
}