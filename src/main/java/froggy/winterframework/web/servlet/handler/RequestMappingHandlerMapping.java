package froggy.winterframework.web.servlet.handler;

import froggy.winterframework.beans.factory.InitializingBean;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.support.ApplicationContextSupport;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.utils.WinterUtils;
import froggy.winterframework.web.bind.annotation.HttpMethod;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.RequestMappingInfo;
import java.lang.reflect.AnnotatedElement;
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
        Map<Method, RequestMappingInfo> methods = selectMethods(handler);
        Object handlerInstance = getApplicationContext().getBean(WinterUtils.resolveSimpleBeanName(handler));

        // 찾은 메소드와 URL 정보를 `registerHandlerMethodMapping()`을 통해 매핑 등록.
        methods.forEach((method, requestMappingInfo) -> {
            registerHandlerMethodMapping(handlerInstance, requestMappingInfo, method);
        });
    }


    /**
     * Handler({@link Controller}) 클래스 내에서 {@link RequestMapping}이 선언된 메소드를 찾아 URL 매핑 정보를 생성.
     *
     * @param handler Handler({@link Controller}) 클래스
     * @return URL 패턴과 해당 메소드 정보를 담은 Map
     */
    private Map<Method, RequestMappingInfo> selectMethods(Class<?> handler) {
        Map<Method, RequestMappingInfo> results = new HashMap<>();

        String baseUrl = extractUrlPattern(handler);
        for (Method method : handler.getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                String methodUrl = extractUrlPattern(method);
                String mappedUrl = combineUrl(baseUrl, methodUrl);

                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                HttpMethod[] httpMethods = requestMapping.httpMethod();

                results.put(method, new RequestMappingInfo(mappedUrl, httpMethods));
            }
        }

        return results;
    }

    /**
     * 주어진 {@link Class} 또는 {@link Method}에 선언된 {@link RequestMapping}에서 urlPattern 값을 추출.
     * 어노테이션이 없는 경우 빈 문자열("")을 반환.
     *
     * @param targetElement  어노테이션이 선언된 {@link Class}나 @{@link Method}
     * @return 추출된 URL 패턴 문자열, 어노테이션이 없으면 ""
     */
    private String extractUrlPattern(AnnotatedElement targetElement) {
        RequestMapping mapping = targetElement .getAnnotation(RequestMapping.class);

        return mapping != null ? mapping.urlPattern() : "";
    }

    /**
     * 클래스 레벨의 Mapping된 URL과 메서드 레벨의 URL Mapping된 URL을 결합.
     *
     * @param baseUrl    클래스 레벨의 URL Mapping
     * @param methodUrl  메서드 레벨의 URL Mapping
     * @return Mapped URL
     */
    private String combineUrl(String baseUrl, String methodUrl) {
        if (baseUrl ==  null) baseUrl = "";
        if (methodUrl == null) methodUrl = "";

        // baseUrl의 마지막 "/" 제거
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 비어있지 않고 '/'로 시작하지 않으면 URL 앞에 '/'를 추가
        if (!methodUrl.isEmpty() && !methodUrl.startsWith("/")) {
            methodUrl = "/" + methodUrl;
        }

        return baseUrl + methodUrl;
    }

    /**
     * URL 패턴과 실행할 메소드를 {@link #registry}에 등록.
     *
     * @param handlerInstance    Handler({@link Controller}) 인스턴스
     * @param requestMappingInfo 매핑 정보 (URL 패턴) {@link RequestMappingInfo}
     * @param method             실행할 {@link Method}
     * @throws IllegalStateException 동일한 URL Pattern과 HTTP Method가 이미 등록된 경우 예외 발생
     */
    private void registerHandlerMethodMapping(Object handlerInstance,
        RequestMappingInfo requestMappingInfo, Method method) throws RuntimeException {
        HandlerMethod handlerMethod = registry.put(requestMappingInfo,
            new HandlerMethod(handlerInstance, method));

        if (handlerMethod != null) {
            throw new IllegalStateException("Duplicate mapping detected: '" + requestMappingInfo.getUrlPattern() + "':(" + handlerMethod.getHandlerInstance().getClass() + ")");
        }
    }

    /**
     * URL 패턴에 해당하는 Handler({@link Controller}) 메소드를 반환.
     *
     * @param urlPattern 요청 URL 패턴
     * @return 매핑된 {@link HandlerMethod}, 매핑이 없으면 {@code null} 반환
     */
    public HandlerMethod getHandlerMethod(String urlPattern, String requestMethod) {
        return registry.get(new RequestMappingInfo(urlPattern, HttpMethod.valueOf(requestMethod)));
    }

}