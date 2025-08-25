package froggy.winterframework.boot.web.servlet.config.annotation;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.annotation.Configuration;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import java.util.Collections;
import java.util.List;

/**
 * 애플리케이션 내에서 정의된 모든 {@link WebMvcConfigurer} Bean을 수집·관리하는 구성 클래스<br>
 *
 * 각 {@link WebMvcConfigurer} 구현체가 정의한 확장 설정을 결합하여
 * {@link WebMvcConfigurationSupport}의 컴포넌트 생성 과정에 순차적으로 적용한다.
 */
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

    private final List<WebMvcConfigurer> configurerComposite;

    @Autowired
    public DelegatingWebMvcConfiguration(ApplicationContext context, List<WebMvcConfigurer> configurerComposite) {
        super(context);
        this.configurerComposite = configurerComposite != null ?
            Collections.unmodifiableList(configurerComposite) :
            Collections.emptyList();
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        for (WebMvcConfigurer configurer : configurerComposite) {
            configurer.addArgumentResolvers(argumentResolvers);
        }
    }

    @Override
    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        for (WebMvcConfigurer configurer : configurerComposite) {
            configurer.addReturnValueHandlers(returnValueHandlers);
        }
    }

}