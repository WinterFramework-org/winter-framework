package froggy.winterframework.web;

import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.utils.WinterUtils;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.servlet.ExceptionResolver;
import froggy.winterframework.web.servlet.HandlerAdapter;
import froggy.winterframework.web.servlet.handler.RequestMappingHandlerMapping;
import froggy.winterframework.web.servlet.mvc.method.annotation.DefaultControllerHandlerAdapter;
import froggy.winterframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import froggy.winterframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Front Controller 역할을 하는 DispatcherServlet.
 *
 * <p>HTTP 요청을 받아 적절한 Handler(Controller) 메소드에 위임,
 * 실행 결과(ModelAndView)를 View로 전달하는 역할을 수행.
 */
@WebServlet(name = "DispatcherServlet", value = "/")
public class DispatcherServlet extends HttpServlet {

    private ApplicationContext context;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private List<HandlerAdapter> handlerAdapters = new ArrayList<>();
    private List<ExceptionResolver> exceptionResolvers = new ArrayList<>();

    public DispatcherServlet(ApplicationContext context) {
        this.context = context;
    }

    /**
     * 기본 전략 객체를 초기화한다.
     */
    @Override
    public void init() {
        initHandlerAdapters();
        initHandlerMapping();
        initExceptionResolvers();
    }

    private void initHandlerAdapters() {
        BeanFactory beanFactory = context.getBeanFactory();

        DefaultControllerHandlerAdapter adapter =
            beanFactory.getBean(
                WinterUtils.resolveSimpleBeanName(DefaultControllerHandlerAdapter.class),
                DefaultControllerHandlerAdapter.class
            );

        handlerAdapters.add(adapter);
    }

    private void initHandlerMapping() {
        BeanFactory beanFactory = context.getBeanFactory();

        requestMappingHandlerMapping =
            beanFactory.getBean(
                WinterUtils.resolveSimpleBeanName(RequestMappingHandlerMapping.class),
                RequestMappingHandlerMapping.class
            );

        try {
            requestMappingHandlerMapping.afterPropertiesSet();
        } catch (RuntimeException e) {
            System.err.println("Critical error during initialization: \n" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 예외를 HTTP 응답으로 변환할 기본 Resolver를 초기화한다.
     */
    private void initExceptionResolvers() {
        exceptionResolvers.add(new ExceptionHandlerExceptionResolver());
        exceptionResolvers.add(new DefaultHandlerExceptionResolver());
    }

    /**
     * HTTP 요청을 처리하는 메소드 (Front Controller 역할)
     *
     * <ol>
     *   <li>정적 자원 요청은 서블릿 컨테이너의 DefaultHandler에 위임</li>
     *   <li>요청 URI에 매핑된 Handler(Controller) 메소드를 실행</li>
     *   <li>예외 발생 시 ExceptionResolver를 통해 대체 결과를 생성</li>
     *   <li>최종 ModelAndView를 기반으로 응답을 완료</li>
     * </ol>
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException      입출력 예외 발생 시
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // 정적 자원 요청은 DefaultHandler로 위임한다.
        if (isStaticResource(request, response)) {
            RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("default");
            if (dispatcher != null) {
                dispatcher.forward(request, response);
                return;
            }
        }

        ModelAndView modelAndView = null;
        Exception dispatchException = null;
        HandlerMethod handlerMethod = null;
        try {
            handlerMethod = requestMappingHandlerMapping.getHandlerMethod(request);
            HandlerAdapter handlerAdapter = getHandlerAdapter(handlerMethod);
            modelAndView = handlerAdapter.handle(request, response, handlerMethod);
        } catch (Exception exception) {
            // Handler 실행 예외는 ExceptionResolver 체인으로 넘긴다.
            dispatchException = exception;
        }

        processDispatchResult(request, response, handlerMethod, modelAndView, dispatchException);
    }

    /**
     * 핸들러 실행 결과 또는 예외 처리 결과를 기준으로 응답을 마무리한다.
     *
     * @param request           HttpServletRequest 객체
     * @param response          HttpServletResponse 객체
     * @param handler           요청을 처리한 핸들러 객체
     * @param modelAndView      핸들러 실행 결과(Model, View 정보를 포함)
     * @param dispatchException dispatch 중 발생한 예외
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException      입출력 예외 발생 시
     */
    private void processDispatchResult(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        ModelAndView modelAndView,
        Exception dispatchException
    )
        throws ServletException, IOException {
        if (dispatchException != null) {
            modelAndView = processHandlerException(request, response, handler, dispatchException);
        }

        if (modelAndView == null) {
            throw new IllegalStateException("handlerAdapter must not return null ModelAndView");
        }

        if (modelAndView.isRequestHandled()) {
            return;
        }

        if (modelAndView.getView() == null || modelAndView.getView().isEmpty()) {
            throw new IllegalStateException("view must not be null");
        }

        bindProcessResult(request, modelAndView);
        render(request, response, modelAndView);
    }

    /**
     * 핸들러 실행 중 발생한 예외를 처리한다.
     *
     * <p>등록된 ExceptionResolver를 순차적으로 호출하여 예외를 처리하고,
     * 처리 결과를 ModelAndView로 반환한다.
     * 예외를 처리하지 못하면 원본 예외를 다시 던진다.
     *
     * @param request   현재 HTTP 요청
     * @param response  현재 HTTP 응답
     * @param handler   예외가 발생한 핸들러 객체
     * @param exception 발생한 예외
     * @return 예외 처리 결과 ModelAndView
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException      입출력 예외 발생 시
     */
    private ModelAndView processHandlerException(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception exception
    ) throws ServletException, IOException {

        if (!response.isCommitted()) {
            // 예외를 처리하지 못하면 다음 resolver로 넘기고, 먼저 처리한 resolver의 결과를 반환한다.
            for (ExceptionResolver resolver : exceptionResolvers) {
                ModelAndView resolvedModelAndView =
                    resolver.resolveException(request, response, handler, exception);
                if (resolvedModelAndView != null) {
                    return resolvedModelAndView;
                }
            }
        }

        if (exception instanceof ServletException) {
            throw (ServletException) exception;
        }

        if (exception instanceof IOException) {
            throw (IOException) exception;
        }

        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }

        throw new ServletException("Handler execution failed", exception);
    }

    /**
     * 핸들러 처리 결과(Model)를 View 렌더링 전에 HttpServletRequest 속성에 바인딩한다.
     *
     * @param request      HttpServletRequest 객체
     * @param modelAndView 바인딩할 ModelAndView
     */
    private void bindProcessResult(HttpServletRequest request, ModelAndView modelAndView) {
        Map<String, Object> model = modelAndView.getModel();
        if (model == null || model.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : model.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * View를 렌더링하는 메서드
     *
     * @param request       HttpServletRequest 객체
     * @param response      HttpServletResponse 객체
     * @param modelAndView  렌더링할 View와 Model을 포함한 객체
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException      입출력 예외 발생 시
     */
    private void render(HttpServletRequest request, HttpServletResponse response,
        ModelAndView modelAndView) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(modelAndView.getView());
        dispatcher.forward(request, response);
    }

    /**
     * 정적 리소스 요청 여부를 확인하고 필요한 MIME 타입을 설정한다.
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @return 정적 리소스 요청이면 {@code true}, 그렇지 않으면 {@code false}
     */
    private boolean isStaticResource(HttpServletRequest request, HttpServletResponse response) {
        String accept = request.getHeader("Accept");
        String requestURI = request.getRequestURI();


        if (requestURI.equals("/") || requestURI.endsWith(".jsp") || requestURI.endsWith(".html")) {
            response.setContentType("text/html"); // MIME 타입 설정
            return true;
        }

        if (accept.startsWith("text/css")) {
            response.setContentType("text/css");
            return true;
        }

        if (accept.startsWith("/css")) {
            response.setContentType("text/css");
            return true;
        }

        if (accept.startsWith("image/")) {
            response.setContentType("image/png");
            return true;
        }

        return false;
    }

    /**
     * 주어진 Handler를 처리할 수 있는 HandlerAdapter를 찾는다.
     *
     * @param handler 요청을 처리할 핸들러 객체
     * @return HandlerAdapter
     */
    private HandlerAdapter getHandlerAdapter(Object handler) {
        for (HandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }

        throw new IllegalArgumentException("No adapter for handler [" + handler.getClass() + "]");
    }
}
