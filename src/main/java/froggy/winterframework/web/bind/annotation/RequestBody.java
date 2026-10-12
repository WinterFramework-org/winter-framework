package froggy.winterframework.web.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {

    /**
     * 요청 본문이 필수인지 여부.
     *
     * <p>기본값은 {@code true}이며, 빈 본문과 JSON null을 HTTP 400으로 거부한다.
     * {@code false}이면 본문 값이 없을 때 {@code null}을 전달한다.
     * 단, 기본형 파라미터는 null을 받을 수 없으므로 본문 값이 없으면 HTTP 400으로 거부한다.
     */
    boolean required() default true;

}
