package froggy.winterframework.context.annotation;

import froggy.winterframework.stereotype.Component;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Bean 메서드와 중첩된 컴포넌트를 빈으로 관리하기 위한 설정 클래스 애노테이션
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Configuration {

}
