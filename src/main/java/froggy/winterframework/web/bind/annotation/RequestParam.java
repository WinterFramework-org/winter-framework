package froggy.winterframework.web.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP 요청 파라미터를 메소드 파라미터에 바인딩하는 어노테이션.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    /**
     * 바인딩할 요청 파라미터의 이름.
     */
    String value();

    /**
     * 파라미터가 필수인지 여부.
     * <p>기본값은 {@code true}이며, {@code true}일 경우 파라미터가 없으면 예외발생.
     */
    boolean required() default true;

    /**
     * 파라미터의 기본값.
     * <p>요청 파라미터가 없거나 비어있을 때 사용될 기본값을 지정.
     */
    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
