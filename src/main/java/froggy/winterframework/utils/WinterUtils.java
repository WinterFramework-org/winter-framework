package froggy.winterframework.utils;

import froggy.winterframework.stereotype.Controller;
import java.lang.annotation.Annotation;

/**
 * 프레임워크에서 공통으로 사용되는 유틸리티 기능을 제공하는 클래스.
 */
public class WinterUtils {

    /**
     * 클래스의 전체 패키지명을 포함한 Bean Name을 반환.
     * (패키지명 + 클래스명을 CamelCase로 변환)
     *
     * @param clazz 대상 클래스
     * @return 패키지를 포함한 Bean Name
     */
    public static String resolveFullBeanName(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        String simpleBeanName = resolveSimpleBeanName(clazz);

        return packageName + "." + simpleBeanName;
    }


    /**
     * 클래스명을 CamelCase로 변환하여 Bean Name을 반환.
     *
     * @param clazz 대상 클래스
     * @return Bean Bean Name
     */
    public static String resolveSimpleBeanName(Class<?> clazz) {
        return toCamelCase(clazz.getSimpleName());
    }

    /**
     * 클래스의 이름을 CamelCase 형식으로 변환.
     *
     * @param classSimpleName 클래스명
     * @return CamelCase로 변환된 문자열
     * @throws IllegalArgumentException 클래스 이름이 null이거나 빈 문자열인 경우
     */
    private static String toCamelCase(String classSimpleName) {
        if (classSimpleName == null || classSimpleName.isEmpty()) {
            throw new IllegalArgumentException("Class name must not be null or empty");
        }

        char firstChar = classSimpleName.charAt(0);
        if (Character.isUpperCase(firstChar)) {
            classSimpleName = Character.toLowerCase(firstChar) + classSimpleName.substring(1);
            return classSimpleName;
        }

        return classSimpleName;
    }

    /**
     * 주어진 객체가 Handler(Controller)인지 판별.
     *
     * @param bean 대상 객체
     * @return Handler(Controller)라면 {@code true}, 그렇지 않으면 {@code false}
     */
    public static boolean isHandler(Object bean) {
        return bean.getClass().isAnnotationPresent(Controller.class);
    }

    /**
     * 주어진 클래스가 특정 애노테이션을 포함하고 있는지 확인.
     *
     * @param clazz 대상 클래스
     * @param targetAnnotation 찾을 애노테이션 타입
     * @return 해당 애노테이션이 존재하면 {@code true}, 그렇지 않으면 {@code false}
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> targetAnnotation) {
        if (clazz.isAnnotationPresent(targetAnnotation)) {
            return true;
        }

        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(targetAnnotation)) {
                return true;
            }
        }

        return false;
    }

}
