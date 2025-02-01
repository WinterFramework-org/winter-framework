package froggy.winterframework.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Bean으로 관리될 클래스를 나타내는 애노테이션.
 *
 * <p>이 애노테이션이 선언된 클래스는 {@code ApplicationContext}에 의해 자동으로 Bean으로 등록되며,
 * 클래스패스 스캔 시 해당 클래스를 찾아 Bean으로 등록.
 * <p>또한, {@code @Component}를 메타 애노테이션으로 사용하는 다른 애노테이션도 스캔 대상에 포함.
 * {@code @Controller}가 이에 해당함.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

}
