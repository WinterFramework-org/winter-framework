package froggy.winterframework.web.method;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HandlerMethod {

    Object handlerInstance;
    private final Method method;
    private final Parameter[] parameters;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;

    private HandlerMethod(Object handlerInstance, Method method, Parameter[] parameters,
        Class<?>[] parameterTypes, Class<?> returnType) {
        this.handlerInstance = handlerInstance;
        this.method = method;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    public HandlerMethod(Object handlerInstance, Method method) {
        this(
            handlerInstance,
            method,
            method.getParameters(),
            method.getParameterTypes(),
            method.getReturnType()
        );
    }

    public Object getHandlerInstance() {
        return handlerInstance;
    }

    public Method getMethod() {
        return method;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

}