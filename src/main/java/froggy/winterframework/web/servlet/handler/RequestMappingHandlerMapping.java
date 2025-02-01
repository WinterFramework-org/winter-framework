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


/**
 * {@link RequestMapping} 애노테이션을 스캔하여
 * 요청 URL과 해당 {@link HandlerMethod}를 매핑하는 클래스.
 *
 * <p>애플리케이션 실행 시 모든 Handler({@link Controller})를 검색하고,
 * URL 패턴과 실행할 메소드를 매핑하여 저장.
 * {@code DispatcherServlet}이 요청을 받을 때 해당 URL에 맞는 적절한 메소드를 찾아 실행.
 */
public class RequestMappingHandlerMapping extends ApplicationContextSupport implements
    InitializingBean {

    private final static Map<RequestMappingInfo, HandlerMethod> registry = new HashMap<>();

    public RequestMappingHandlerMapping(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    /**
     * {@link InitializingBean} 인터페이스 구현.
     * <p>Bean 생성 후 초기화 로직을 수행, Handler({@link Controller}) Bean을 스캔하여
     * {@link RequestMapping} 정보를 등록.
     */
    @Override
    public void afterPropertiesSet() {
        initHandlerMethods();
    }

    /**
     * Handler({@link Controller}) Bean을 스캔하고, URL 매핑을 등록.
     */
    private void initHandlerMethods() {
        BeanFactory beanFactory = getApplicationContext().getBeanFactory();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            processCandidateBean(beanName, beanFactory);
        }
    }

    /**
     * 지정된 Bean이 Handler({@link Controller})인지 확인 후, {@link #registry}에 등록
     *
     * @param beanName    Bean 이름
     * @param beanFactory BeanFactory 인스턴스
     */
    private void processCandidateBean(String beanName, BeanFactory beanFactory) {
        Object bean = beanFactory.getBean(beanName);

        if (WinterUtils.isHandler(bean)) {
            Class<?> beanType = beanFactory.getBeanDefinition(beanName).getBeanClass();

            detectHandlerMethods(beanType);
        }
    }

    /**
     * 주어진 Handler({@link Controller}) 클래스에서 {@link RequestMapping}이 선언된 메소드를 찾은 후 매핑등록 요청
     *
     * @param handler Handler({@link Controller}) 클래스
     */
    public void detectHandlerMethods(Class<?> handler) {
        // 해당 클래스에서 `@RequestMapping`이 붙은 메소드를 찾음.
        Map<RequestMappingInfo, Method> methods = selectMethods(handler);
        Object handlerInstance = getApplicationContext().getBean(WinterUtils.resolveSimpleBeanName(handler));

        // 찾은 메소드와 URL 정보를 `registerHandlerMethodMapping()`을 통해 매핑 등록.
        methods.forEach((requestMappingInfo, method) -> {
            registerHandlerMethodMapping(handlerInstance, requestMappingInfo, method);
        });
    }


    /**
     * Handler({@link Controller}) 클래스 내에서 {@link RequestMapping}이 선언된 메소드를 찾아 URL 매핑 정보를 생성.
     *
     * @param handler Handler({@link Controller}) 클래스
     * @return URL 패턴과 해당 메소드 정보를 담은 Map
     */
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

    /**
     * URL 패턴과 실행할 메소드를 {@link #registry}에 등록.
     *
     * @param handlerInstance    Handler({@link Controller}) 인스턴스
     * @param requestMappingInfo 매핑 정보 (URL 패턴) {@link RequestMappingInfo}
     * @param method             실행할 {@link Method}
     */
    private void registerHandlerMethodMapping(Object handlerInstance,
        RequestMappingInfo requestMappingInfo, Method method) {
        registry.put(requestMappingInfo, new HandlerMethod(handlerInstance, method));
    }

    /**
     * URL 패턴에 해당하는 Handler({@link Controller}) 메소드를 반환.
     *
     * @param urlPattern 요청 URL 패턴
     * @return 매핑된 {@link HandlerMethod}, 매핑이 없으면 {@code null} 반환
     */
    public HandlerMethod getHandlerMethod(String urlPattern) {
        return registry.get(new RequestMappingInfo(urlPattern));
    }

}