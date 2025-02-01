package froggy.winterframework.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 웹 요청을 처리하는 Handler(Controller)를 나타내는 애노테이션.
 *
 * <p>이 애노테이션이 선언된 클래스는 HTTP 요청을 처리하는 Handler(Controller)로 등록되며
 * {@link Component}를 포함하여 Bean으로 자동 등록
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Controller {
    String name() default "";
    String url() default "";

}
