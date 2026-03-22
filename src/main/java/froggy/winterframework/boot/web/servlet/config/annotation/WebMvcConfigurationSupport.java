package froggy.winterframework.boot.web.servlet.config.annotation;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.annotation.Bean;
import froggy.winterframework.validation.LocalValidatorFactoryBean;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import froggy.winterframework.web.servlet.ExceptionResolver;
import froggy.winterframework.web.servlet.handler.HandlerExceptionResolverComposite;
import froggy.winterframework.web.servlet.handler.RequestMappingHandlerMapping;
import froggy.winterframework.web.servlet.mvc.method.annotation.DefaultControllerHandlerAdapter;
import froggy.winterframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import froggy.winterframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import java.util.LinkedList;
import java.util.List;

/**
 * Web MVC의 핵심 컴포넌트를 빈(Bean)으로 등록하는 구성 클래스<br>
 * 핵심 컴포넌트를 생성하고, 사용자 정의를 위한 확장 포인트를 제공한다.
 */
public class WebMvcConfigurationSupport {

    protected final ApplicationContext context;
    private final List<HandlerMethodArgumentResolver> argumentResolvers = new LinkedList<>();
    private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new LinkedList<>();

    @Autowired
    public WebMvcConfigurationSupport(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = createRequestMappingHandlerMapping();

        return mapping;
    }

    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping(context);
    }

    @Bean
    public DefaultControllerHandlerAdapter defaultControllerHandlerAdapter() {
        DefaultControllerHandlerAdapter adapter = createDefaultControllerHandlerAdapter();

        adapter.addArgumentResolvers(getArgumentResolvers());
        adapter.addReturnValueHandlers(getReturnValueHandlers());
        return adapter;
    }

    protected DefaultControllerHandlerAdapter createDefaultControllerHandlerAdapter() {
        LocalValidatorFactoryBean validatorFactoryBean = context.getBeanFactory()
            .getBean("localValidatorFactoryBean", LocalValidatorFactoryBean.class);
        return new DefaultControllerHandlerAdapter(validatorFactoryBean);
    }

    protected ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
        ExceptionHandlerExceptionResolver resolver = createExceptionHandlerExceptionResolver();
        resolver.addArgumentResolvers(getArgumentResolvers());
        resolver.addReturnValueHandlers(getReturnValueHandlers());
        return resolver;
    }

    protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver() {
        return new ExceptionHandlerExceptionResolver();
    }

    protected DefaultHandlerExceptionResolver defaultHandlerExceptionResolver() {
        return createDefaultHandlerExceptionResolver();
    }

    protected DefaultHandlerExceptionResolver createDefaultHandlerExceptionResolver() {
        return new DefaultHandlerExceptionResolver();
    }

    @Bean
    public ExceptionResolver exceptionResolverComposite() {
        HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
        List<ExceptionResolver> resolvers = new LinkedList<>();

        configureHandlerExceptionResolvers(resolvers);

        if (resolvers.isEmpty()) {
            addDefaultHandlerExceptionResolvers(resolvers);
        }

        composite.setExceptionResolvers(resolvers);
        return composite;
    }

    protected void addDefaultHandlerExceptionResolvers(List<ExceptionResolver> resolvers) {
        resolvers.add(exceptionHandlerExceptionResolver());
        resolvers.add(defaultHandlerExceptionResolver());
    }

    protected void configureHandlerExceptionResolvers(List<ExceptionResolver> resolvers) {
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean(context.getBeanFactory());
    }

    public List<HandlerMethodArgumentResolver> getArgumentResolvers() {
        if (!argumentResolvers.isEmpty()) return argumentResolvers;

        addArgumentResolvers(argumentResolvers);
        return argumentResolvers;
    }

    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    public List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
        if (!returnValueHandlers.isEmpty()) return returnValueHandlers;

        addReturnValueHandlers(returnValueHandlers);
        return returnValueHandlers;
    }

    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

}
