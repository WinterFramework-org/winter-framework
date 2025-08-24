package froggy.winterframework.web;

import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.utils.WinterUtils;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.servlet.HandlerAdapter;
import froggy.winterframework.web.servlet.handler.RequestMappingHandlerMapping;
import froggy.winterframework.web.servlet.mvc.method.annotation.DefaultControllerHandlerAdapter;
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

    public DispatcherServlet(ApplicationContext context) {
        this.context = context;
    }

    /**
     * URL과 Handler(Controller) 메소드를 매핑하는 RequestMappingHandlerMapping을 초기화
     *
     * <p>DispatcherServlet 객체가 생성되면,
     * 등록된 모든 Handler(Controller) Bean을 스캔하여 URL과 매핑 정보를 설정
     */
    @Override
    public void init() {
        initHandlerAdapters();
        initHandlerMapping();
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
     * HTTP 요청을 처리하는 메소드 (FrontController 역할)
     *
     * <ol>
     *   <li>정적 자원 요청은 서블릿 컨테이너의 DefaultHandler에 위임</li>
     *   <li>요청 URI에 매핑된 Handler(Controller) 메소드를 실행</li>
     *   <li>실행 결과({@link ModelAndView})를 request 속성에 저장하고, View로 전달</li>
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

        // 정적 자원 처리: 요청이 정적 자원에 해당하면 DefaultHandler로 위임
        if (isStaticResource(request, response)) {
            RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("default");
            if (dispatcher != null) {
                dispatcher.forward(request, response);
                return;
            }
        }

        // 요청 URI에 매핑되는 Handler(Controller) 메소드 객체 찾기
        HandlerMethod handlerMethod = requestMappingHandlerMapping.getHandlerMethod(request);

        ModelAndView modelAndView = null;
        HandlerAdapter handlerAdapter = getHandlerAdapter(handlerMethod);
        try {
            modelAndView = handlerAdapter.handle(request, response, handlerMethod);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        processDispatchResult(request, response, modelAndView);
    }

    /**
     * 컨트롤러의 처리 결과(ModelAndView)를 기반으로,
     * View 렌더링 전 필요한 후처리 및 렌더링을 수행한다.
     *
     * @param request       HttpServletRequest 객체
     * @param response      HttpServletResponse 객체
     * @param modelAndView  핸들러 실행 결과(Model, View 정보를 포함)
     */
    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView)
        throws ServletException, IOException {
        if (modelAndView.isRequestHandled()) {
            return;
        }

        if (modelAndView.getView() == null || modelAndView.getView().isEmpty()) {
            throw new IllegalStateException("view must not be null");
        }

        //  ModelAndView 데이터를 request 속성에 추가
        bindProcessResult(request, modelAndView);

        // view 렌더링
        render(request, response, modelAndView);
    }

    /**
     * 핸들러를 통해 처리된 결과를 View 렌더링 전에 HttpServletRequest에 바인딩한다.
     *
     * @param request
     * @param modelAndView
     */
    private void bindProcessResult(HttpServletRequest request, ModelAndView modelAndView) {
        Map<String, Object> model = modelAndView.getModel();
        if (model == null || model.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry: model.entrySet()) {
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
     * 정적 리소스 요청을 확인하는 메소드
     *
     * <p>요청 URI 및 Accept 헤더 정보를 기반으로 정적 자원(HTML, JSP, CSS, 이미지 등)에 해당하는지 확인하고,
     * 응답의 MIME 타입을 설정
     *
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
     * 주어진 Handler(Controller)를 처리할 수 있는 {@link HandlerAdapter}를 찾는다
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
