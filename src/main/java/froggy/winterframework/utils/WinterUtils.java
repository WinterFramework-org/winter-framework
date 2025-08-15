package froggy.winterframework.utils;

import froggy.winterframework.stereotype.Controller;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

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
     * 주어진 Element에 특정 애노테이션을 포함하고 있는지 확인.
     *
     * @param targetElement 대상 Element (클래스, 메서드, 생성자, 필드 등등)
     * @param targetAnnotation 찾을 애노테이션 타입
     * @return 해당 애노테이션이 존재하면 {@code true}, 그렇지 않으면 {@code false}
     */
    public static boolean hasAnnotation(AnnotatedElement targetElement, Class<? extends Annotation> targetAnnotation) {
        if (targetElement.isAnnotationPresent(targetAnnotation)) {
            return true;
        }

        for (Annotation annotation : targetElement.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(targetAnnotation)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 지정한 패키지들(candidatePackages)에서
     * targetAnnotation이 선언(직접 또는 메타 애노테이션)된 클래스를 스캔해,
     * 매칭된 클래스들의 Class<?> 객체(Set)를 반환한다.
     *
     * @param targetAnnotation  스캔 대상 애노테이션 클래스
     * @param candidatePackages 스캔할 루트 패키지 이름들 (예: "com.example.app", "org.lib")
     * @return 매칭된 클래스들의 {@code Set<Class<?>>}
     */
    public static Set<Class<?>> scanTypesAnnotatedWith(Class<? extends Annotation> targetAnnotation, String... candidatePackages) {
        Set<Class<?>> results = new HashSet<>();

        for (String candidatePackage : candidatePackages) {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(candidatePackage))
                .setScanners(new SubTypesScanner(false))
            );

            results.addAll(reflections.getSubTypesOf(Object.class));
        }

        return results.stream()
            .filter(clazz -> hasAnnotation(clazz, targetAnnotation))
            .filter(clazz -> !clazz.isAnnotation())
            .collect(Collectors.toSet());
    }

}
