package froggy.winterframework.web.servlet.handler;

import froggy.winterframework.beans.factory.InitializingBean;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.support.ApplicationContextSupport;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.utils.WinterUtils;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.RequestMappingInfo;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestMappingHandlerMapping extends ApplicationContextSupport implements
    InitializingBean {

    private final static Map<RequestMappingInfo, HandlerMethod> registry = new HashMap<>();

    public RequestMappingHandlerMapping(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void afterPropertiesSet() {
        initHandlerMethods();
    }

    private void initHandlerMethods() {
        BeanFactory beanFactory = getApplicationContext().getBeanFactory();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            processCandidateBean(beanName, beanFactory);
        }
    }

    private void processCandidateBean(String beanName, BeanFactory beanFactory) {
        Object bean = beanFactory.getBean(beanName);

        if (WinterUtils.isHandler(bean)) {
            Class<?> beanType = beanFactory.getBeanDefinition(beanName).getBeanClass();

            detectHandlerMethods(beanType);
        }
    }

    public void detectHandlerMethods(Class<?> handler) {
        Map<RequestMappingInfo, Method> methods = selectMethods(handler);
        Object handlerInstance = getApplicationContext().getBean(WinterUtils.resolveSimpleBeanName(handler));

        methods.forEach((requestMappingInfo, method) -> {
            registerHandlerMethodMapping(handlerInstance, requestMappingInfo, method);
        });
    }


    private Map<RequestMappingInfo, Method> selectMethods(Class<?> handler) {
        Map<RequestMappingInfo, Method> results = new HashMap<>();

        Controller handlerAnnotation = handler.getAnnotation(Controller.class);
        for (Method method : handler.getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                String urlPattern = handlerAnnotation.url() + methodAnnotation.urlPattern();

                results.put(new RequestMappingInfo(urlPattern), method);
            }
        }

        return results;
    }

    private void registerHandlerMethodMapping(Object handlerInstance,
        RequestMappingInfo requestMappingInfo, Method method) {
        registry.put(requestMappingInfo, new HandlerMethod(handlerInstance, method));
    }

    public HandlerMethod getHandlerMethod(String urlPattern) {
        return registry.get(new RequestMappingInfo(urlPattern));
    }

}