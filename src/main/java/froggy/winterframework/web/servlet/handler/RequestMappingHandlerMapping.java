package froggy.winterframework.web.servlet.handler;

import froggy.winterframework.beans.factory.InitializingBean;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.support.ApplicationContextSupport;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.utils.WinterUtils;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.RequestMappingInfo;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

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

    private final MappingRegistry mappingRegistry = new MappingRegistry();

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
     * 지정된 Bean이 Handler({@link Controller})인지 확인 후, {@link #mappingRegistry}에 등록
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
     * @param handlerType Handler({@link Controller}) 클래스
     */
    public void detectHandlerMethods(Class<?> handlerType) {
        // 해당 클래스에서 `@RequestMapping`이 붙은 메소드를 찾음.
        Map<Method, RequestMappingInfo> methods = selectMethods(handlerType);


        // 찾은 메소드와 URL 정보를 `registerHandlerMethodMapping()`을 통해 매핑 등록.
        methods.forEach((method, requestMappingInfo) -> {
            registerHandlerMethodMapping(handlerType, requestMappingInfo, method);
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

        RequestMappingInfo baseMapping = createRequestMappingInfo(handler);
        for (Method method : handler.getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMappingInfo methodMapping = createRequestMappingInfo(method);

                // 클래스 레벨 매핑 정보와 메서드 레벨 매핑 정보를 결합하여 최종 RequestMappingInfo 생성
                RequestMappingInfo requestInfo = baseMapping.combine(methodMapping);
                results.put(method,requestInfo);
            }
        }

        return results;
    }

    /**
     * 주어진 어노테이션 요소에서 {@link RequestMapping} 정보를 생성.
     *
     * @param e 어노테이션이 적용된 요소
     * @return RequestMapping 어노테이션 기반의 RequestMappingInfo 객체, 또는 빈 객체
     */
    protected RequestMappingInfo createRequestMappingInfo(AnnotatedElement e) {
        if (!e.isAnnotationPresent(RequestMapping.class)) {
            return RequestMappingInfo.emptyRequestMappingInfo();
        }
        RequestMapping annotation = e.getAnnotation(RequestMapping.class);

        return new RequestMappingInfo(annotation.value(), annotation.method());
    }

    /**
     * 주어진 {@link Class} 또는 {@link Method}에 선언된 {@link RequestMapping}에서 urlPattern 값을 추출.
     * 어노테이션이 없는 경우 빈 문자열("")을 반환.
     *
     * @param targetElement  어노테이션이 선언된 {@link Class}나 @{@link Method}
     * @return 추출된 URL 패턴 문자열, 어노테이션이 없으면 ""
     */
    private String extractUrlPattern(AnnotatedElement targetElement) {
        RequestMapping mapping = targetElement.getAnnotation(RequestMapping.class);

        return mapping != null ? mapping.value() : "";
    }

    /**
     * URL 패턴과 실행할 메소드를 {@link #mappingRegistry}에 등록.
     *
     * @param handlerType    Handler({@link Controller}) Type
     * @param requestMappingInfo 매핑 정보 (URL 패턴) {@link RequestMappingInfo}
     * @param method             실행할 {@link Method}
     * @throws IllegalStateException 동일한 URL Pattern과 HTTP Method가 이미 등록된 경우 예외 발생
     */
    private void registerHandlerMethodMapping(Class<?> handlerType,
        RequestMappingInfo requestMappingInfo, Method method) throws RuntimeException {

        Object handlerInstance = getApplicationContext().getBean(WinterUtils.resolveSimpleBeanName(handlerType));
        HandlerMethod handlerMethod = mappingRegistry.addMappings(
            requestMappingInfo, new HandlerMethod(handlerInstance, handlerType, method));

        if (handlerMethod != null) {
            throw new IllegalStateException("Duplicate mapping detected: '" + requestMappingInfo.getUrlPattern() + "':(" + handlerMethod.getHandlerInstance().getClass() + ")");
        }
    }

    /**
     * URL 패턴에 해당하는 Handler({@link Controller}) 메소드를 반환.
     *
     * @param request Request객체
     * @return 매핑된 {@link HandlerMethod}, 매핑이 없으면 {@code null} 반환
     */
    public HandlerMethod getHandlerMethod(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        HandlerMethod handlerMethod = lookupHandlerMethod(requestURI, requestMethod);
        if (handlerMethod == null) {
            throw new IllegalStateException("No matching handler method found for request [URI: " + requestURI + ", Method: " + requestMethod + "]");
        }

        extractPathVariables(handlerMethod, request);

        return handlerMethod;
    }

    private HandlerMethod lookupHandlerMethod(String requestURI, String requestMethod) {
        HandlerMethod directPathMatch =
            mappingRegistry.getMappingsByDirectPath(requestURI, requestMethod);

        if (directPathMatch != null) return directPathMatch;

        return mappingRegistry.getMappingsByPathVariable(requestURI, requestMethod);
    }

    private void extractPathVariables(HandlerMethod handlerMethod, HttpServletRequest request) {
        if (!handlerMethod.hasPathVariable()) return;

        String urlPattern = extractUrlPattern(handlerMethod.getHandlerType()) + extractUrlPattern(handlerMethod.getMethod());
        String[] urlPatternParts = urlPattern.split("/");
        String[] requestURIParts = request.getRequestURI().split("/");

        HashMap<String, String> pathVariableMap = new HashMap<>();
        for (int i = 0; i < urlPatternParts.length; i++) {
            if (!urlPatternParts[i].equals(requestURIParts[i])) {
                String key = urlPatternParts[i].substring(1, urlPatternParts[i].length() - 1);
                pathVariableMap.put(key, requestURIParts[i]);
            }
        }

        request.setAttribute("uriTemplateVariables", pathVariableMap);
    }

    class MappingRegistry {
        private final Map<RequestMappingInfo, HandlerMethod> directPathHandlerMap = new HashMap<>();
        private final Map<RequestMappingInfo, HandlerMethod> pathVariableHandlerMap = new HashMap<>();

        public HandlerMethod getMappingsByDirectPath(String requestURI, String requestMethod) {
            return directPathHandlerMap.get(new RequestMappingInfo(requestURI, RequestMethod.valueOf(requestMethod)));
        }

        public HandlerMethod addMappings(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
            if (handlerMethod.hasPathVariable()) {
                return pathVariableHandlerMap.put(requestMappingInfo, handlerMethod);
            }

            return directPathHandlerMap.put(requestMappingInfo, handlerMethod);
        }

        public HandlerMethod getMappingsByPathVariable(String requestURI, String requestMethod) {
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : pathVariableHandlerMap.entrySet()) {
                if (isMatchingPattern(entry.getKey().getUrlPattern(), requestURI) && isMatchingMethod(entry.getKey().getHttpMethods(), requestMethod)) {
                    return entry.getValue();
                }
            }

            return null;
        }

        private boolean isMatchingPattern(String pattern, String requestURI) {
            String[] patternParts = pattern.split("/");
            String[] requestParts = requestURI.split("/");

            if (patternParts.length != requestParts.length) return false;

            for (int i = 0; i < patternParts.length; i++) {
                boolean isVariablePattern = patternParts[i].startsWith("{") && patternParts[i].endsWith("}");
                if (!isVariablePattern && !patternParts[i].equals(requestParts[i])) {
                    return false;
                }
            }

            return true;
        }

        private boolean isMatchingMethod(Set<RequestMethod> requestMethods, String requestMethod) {
            HashSet<RequestMethod> requestRequestMethod = new HashSet<>(Collections.singleton(RequestMethod.valueOf(requestMethod)));
            return !Collections.disjoint(requestMethods, requestRequestMethod);
        }
    }
}