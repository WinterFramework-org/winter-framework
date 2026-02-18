package froggy.winterframework.context.annotation;

import froggy.winterframework.beans.factory.config.ScopeType;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Annotation 기반으로 Scope 메타데이터를 해석.
 */
public class AnnotationScopeMetadataResolver {

    /**
     * Component 클래스의 Scope를 해석.
     */
    public ScopeType resolveScopeMetadata(Class<?> targetClass) {
        return resolveScopeMetadata((AnnotatedElement) targetClass);
    }

    /**
     * @Bean FactoryMethod의 Scope를 해석.
     */
    public ScopeType resolveScopeMetadata(Method factoryMethod) {
        return resolveScopeMetadata((AnnotatedElement) factoryMethod);
    }

    private ScopeType resolveScopeMetadata(AnnotatedElement targetElement) {
        if (targetElement == null) {
            throw new IllegalArgumentException("targetElement must not be null");
        }

        Scope scope = findScopeAnnotation(targetElement);
        if (scope == null || scope.value() == null) {
            return ScopeType.SINGLETON;
        }

        return scope.value();
    }

    private Scope findScopeAnnotation(AnnotatedElement targetElement) {
        Scope directScope = targetElement.getAnnotation(Scope.class);
        if (directScope != null) {
            return directScope;
        }

        for (Annotation annotation : targetElement.getAnnotations()) {
            Scope metaScope = annotation.annotationType().getAnnotation(Scope.class);
            if (metaScope != null) {
                return metaScope;
            }
        }

        return null;
    }
}
