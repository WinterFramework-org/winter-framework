package froggy.winterframework.web;

import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.servlet.handler.RequestMappingHandlerMapping;
import java.io.IOException;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DispatcherServlet", value = "/")
public class DispatcherServlet extends HttpServlet {

    private ApplicationContext context;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public DispatcherServlet(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void init() {
        requestMappingHandlerMapping = (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
        requestMappingHandlerMapping.afterPropertiesSet();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        /*
        * 정적 자원 처리
        * Jetty의 DefaultHandler로 요청 전달
        * TODO: Tomcat WAS 서버도 호환가능하게
        * */
        if (isStaticResource(request, response)) {
            RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("default");
            if (dispatcher != null) {
                dispatcher.forward(request, response);
                return;
            }
        }

        /**
         * 요청 URI와 매핑되는 컨트롤러 메소드 실행
         * TODO: 추후 URI에 따라 메소드도 동적으로 매핑되도록 개선 필요
         */
        String requestURI = request.getRequestURI();

        ModelAndView modelAndView = null;
        HandlerMethod handlerMethod = requestMappingHandlerMapping.getHandlerMethod(requestURI);
        Object instance = handlerMethod.getHandlerInstance();

        try {
            modelAndView = (ModelAndView) handlerMethod.getMethod().invoke(instance, request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ModelAndView의 데이터를 Request에 추가
        if (!modelAndView.getModel().isEmpty()) {
            for (Map.Entry<String, Object> entry: modelAndView.getModel().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        // 렌더링
        RequestDispatcher dispatcher = request.getRequestDispatcher(modelAndView.getView());
        dispatcher.forward(request, response);
    }

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
}
