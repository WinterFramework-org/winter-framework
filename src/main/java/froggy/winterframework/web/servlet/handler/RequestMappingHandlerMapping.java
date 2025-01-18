package froggy.winterframework.web.servlet.handler;

import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.RequestMappingInfo;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestMappingHandlerMapping {

    private final static Map<RequestMappingInfo, HandlerMethod> registry = new HashMap<>();

    public void detectHandlerMethods(Class<?> handler) {
        Map<RequestMappingInfo, Method> methods = selectMethods(handler);

        methods.forEach((requestMappingInfo, method) -> {
            registerHandlerMethodMapping(handler, requestMappingInfo, method);
        });
    }


    private Map<RequestMappingInfo, Method> selectMethods(Class<?> handler) {
        Map<RequestMappingInfo, Method> methods = new HashMap<>();

        Controller handlerAnnotation = handler.getAnnotation(Controller.class);
        for (Method method : handler.getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                String urlPattern = handlerAnnotation.url() + methodAnnotation.urlPattern();;

                methods.put(new RequestMappingInfo(urlPattern), method);
            }
        }

        return methods;
    }
    private void registerHandlerMethodMapping(Class<?> handler, RequestMappingInfo requestMappingInfo, Method method) {
        registry.put(requestMappingInfo, new HandlerMethod(handler, method));
    }

    public static HandlerMethod getHandlerMethod(String urlPattern) {
        return registry.get(new RequestMappingInfo(urlPattern));
    }
}