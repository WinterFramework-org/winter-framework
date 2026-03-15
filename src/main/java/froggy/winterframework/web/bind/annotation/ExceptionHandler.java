package froggy.winterframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드를 예외 처리 메서드로 선언한다.
 *
 * <p>이 어노테이션이 붙은 메서드는 컨트롤러 실행 중 발생한 예외를 처리하는 용도로 사용된다.
 * 처리 대상 예외는 {@link #value()}에 선언하며, 값이 비어 있으면 메서드 파라미터의
 * 예외 타입을 기준으로 처리 대상을 결정한다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandler {

    /**
     * 처리할 예외 타입을 속성 이름 없이 선언한다.
     *
     * <p>예: {@code @ExceptionHandler(IllegalArgumentException.class)}
     */
    Class<? extends Throwable>[] value() default {};
}
