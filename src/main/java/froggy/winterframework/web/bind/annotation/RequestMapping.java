package froggy.winterframework.web.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP 요청 URL과 {@code HandlerMethod}를 매핑하는 애노테이션.
 *
 * <p>클래스 레벨에서는 기본 URL을 정의하고, 메소드 레벨에서는 구체적인 경로를 설정하여
 * 요청과 해당 {@code HandlerMethod}간의 매핑을 지원
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface  RequestMapping {
    String urlPattern() default "";
    HttpMethod[] httpMethod() default {};
}
