package froggy.winterframework.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 핸들러 메서드의 파라미터 정보를 래핑하는 클래스.
 *
 * <p>
 * {@link Parameter}와 함께 해당 파라미터가 속한 {@link Method}와
 * 인덱스를 보관하여, Resolver가 메서드 전체 컨텍스트에 접근할 수 있게 한다.
 * </p>
 */
public class MethodParameter {

    private final Method method;
    private final int parameterIndex;
    private final Parameter parameter;

    public MethodParameter(Method method, int parameterIndex) {
        if (parameterIndex < 0 || parameterIndex >= method.getParameterCount()) {
            throw new IllegalArgumentException(
                    "Parameter index [" + parameterIndex + "] out of bounds for method: "
                            + method.getDeclaringClass().getSimpleName() + "#" + method.getName()
                            + " (parameter count: " + method.getParameterCount() + ")");
        }
        this.method = method;
        this.parameterIndex = parameterIndex;
        this.parameter = method.getParameters()[parameterIndex];
    }

    /**
     * 주어진 메서드의 모든 파라미터를 {@link MethodParameter} 배열로 변환한다.
     *
     * @param method 대상 메서드
     * @return MethodParameter 배열
     */
    public static MethodParameter[] forMethod(Method method) {
        Parameter[] parameters = method.getParameters();
        MethodParameter[] result = new MethodParameter[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            result[i] = new MethodParameter(method, i);
        }

        return result;
    }

    /**
     * 지정한 인덱스의 파라미터를 {@link MethodParameter}로 반환한다.
     *
     * @param index 파라미터 인덱스
     * @return 해당 인덱스의 MethodParameter
     */
    public MethodParameter getMethodParameter(int index) {
        return new MethodParameter(method, index);
    }

    /**
     * 파라미터에 지정된 어노테이션이 존재하는지 확인한다.
     *
     * @param annotationType 확인할 어노테이션 타입
     * @return 존재하면 {@code true}
     */
    public <A extends Annotation> boolean hasParameterAnnotation(Class<A> annotationType) {
        return parameter.isAnnotationPresent(annotationType);
    }

    /**
     * 파라미터에 선언된 어노테이션을 반환한다.
     *
     * @param annotationType 조회할 어노테이션 타입
     * @return 어노테이션 인스턴스, 없으면 {@code null}
     */
    public <A extends Annotation> A getParameterAnnotation(Class<A> annotationType) {
        return parameter.getAnnotation(annotationType);
    }

    /**
     * 파라미터의 타입을 반환한다.
     */
    public Class<?> getParameterType() {
        return parameter.getType();
    }

    /**
     * 파라미터의 이름을 반환한다.
     */
    public String getParameterName() {
        return parameter.getName();
    }

    /**
     * 해당 파라미터가 속한 메서드를 반환한다.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 파라미터의 인덱스를 반환한다.
     */
    public int getParameterIndex() {
        return parameterIndex;
    }

    /**
     * 메서드의 전체 파라미터 개수를 반환한다.
     */
    public int getParameterCount() {
        return method.getParameterCount();
    }

}
