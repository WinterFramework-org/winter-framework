package froggy.winterframework.web.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 해당 파라매터가 Web Request Parameter와 바인딩되어야 함을 표시
 *
 * <p>해당 애노테이션이 적용된 매개변수는 Request Parameter에서 값을 추출하여 자동으로 매핑됨.</p>
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    /**
     * 요청에서 매핑할 파라미터 이름.
     *
     * @return 바인딩할 요청 파라미터 이름
     */
    String value();
}
