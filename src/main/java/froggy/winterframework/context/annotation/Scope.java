package froggy.winterframework.context.annotation;

import froggy.winterframework.beans.factory.config.ScopeType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean Scope 메타데이터.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    ScopeType value() default ScopeType.SINGLETON;
}
