package froggy.winterframework.web.servlet;

import froggy.winterframework.web.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 요청을 처리할 적절한 핸들러 어댑터를 결정하고 실행하는 인터페이스.
 * 다양한 핸들러를 지원하기 위해 사용됨.
 */
public interface HandlerAdapter {

    /**
     * 주어진 핸들러가 해당 어댑터에서 지원 가능한지 확인.
     *
     * @param handler 검사할 핸들러 객체
     * @return 지원 한다면 {@code true}, 그렇지 않으면 {@code false}
     */
    boolean supports(Object handler);

    /**
     * 요청을 처리하고 응답을 생성
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler  요청을 처리할 핸들러 객체
     * @return 처리 결과를 포함한 ModelAndView
     * @throws Exception 핸들러 실행 중 예외
     */
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
}
