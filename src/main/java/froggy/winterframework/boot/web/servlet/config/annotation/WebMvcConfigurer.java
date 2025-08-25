package froggy.winterframework.boot.web.servlet.config.annotation;

import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import java.util.List;

/**
 * MVC 컴포넌트를 확장하기 위한 인터페이스<br>
 * 구현한 Bean은 자동으로 감지되어 관련 컴포넌트 설정에 적용된다.
 */
public interface WebMvcConfigurer {

    /**
     * 사용자 정의 HandlerMethodArgumentResolver를 추가
     * @param argumentResolvers 현재 등록된 ArgumentResolver
     */
    default void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    /**
     * 사용자 정의 HandlerMethodReturnValueHandler를 추가
     * @param returnValueHandlers 현재 등록된 ReturnValueHandler
     */
    default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }
}
