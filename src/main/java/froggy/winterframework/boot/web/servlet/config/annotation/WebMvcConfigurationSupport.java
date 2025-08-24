package froggy.winterframework.boot.web.servlet.config.annotation;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.annotation.Bean;
import froggy.winterframework.context.annotation.Configuration;
import froggy.winterframework.web.servlet.handler.RequestMappingHandlerMapping;
import froggy.winterframework.web.servlet.mvc.method.annotation.DefaultControllerHandlerAdapter;

@Configuration
public class WebMvcConfigurationSupport {

    protected final ApplicationContext context;

    @Autowired
    public WebMvcConfigurationSupport(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return createRequestMappingHandlerMapping();
    }

    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping(context);
    }

    @Bean
    public DefaultControllerHandlerAdapter defaultControllerHandlerAdapter() {
        return createDefaultControllerHandlerAdapter();
    }

    protected DefaultControllerHandlerAdapter createDefaultControllerHandlerAdapter() {
        return new DefaultControllerHandlerAdapter();
    }

}
