package froggy.winterframework.beans.factory.annotation;

import froggy.winterframework.core.env.Environment;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 프로퍼티 값을 주입할 때 사용되는 애노테이션.
 *
 * Bean 생성 과정에서 {@link #value()}에 지정된 값을 {@link Environment}에서 조회하여 값을 주입함
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    String value();
}
