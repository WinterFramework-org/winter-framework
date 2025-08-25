package froggy.winterframework.boot.web.servlet.config.annotation;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.annotation.Bean;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import froggy.winterframework.web.servlet.handler.RequestMappingHandlerMapping;
import froggy.winterframework.web.servlet.mvc.method.annotation.DefaultControllerHandlerAdapter;
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
        return new DefaultControllerHandlerAdapter();
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
