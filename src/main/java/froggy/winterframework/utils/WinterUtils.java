package froggy.winterframework.utils;

import froggy.winterframework.stereotype.Controller;
import java.lang.annotation.Annotation;

public class WinterUtils {

    public static String resolveFullBeanName(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        String simpleBeanName = resolveSimpleBeanName(clazz);

        return packageName + "." + simpleBeanName;
    }

    public static String resolveSimpleBeanName(Class<?> clazz) {
        return toCamelCase(clazz.getSimpleName());
    }

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

    public static boolean isHandler(Object bean) {
        return bean.getClass().isAnnotationPresent(Controller.class);
    }

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
