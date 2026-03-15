package froggy.winterframework.web.servlet;

import froggy.winterframework.web.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 핸들러 실행 중 발생한 예외를 처리하는 인터페이스.
 *
 * 구현체는 예외를 처리하고 적절한 ModelAndView를 반환한다.
 */
public interface ExceptionResolver {

    /**
     * 주어진 예외를 처리하고 예외 처리 결과 ModelAndView를 구성해 반환한다.
     *
     * <p>처리하지 못한 경우 {@code null}을 반환하여 다음 Resolver로 위임한다.
     *
     * @param request   현재 HTTP 요청
     * @param response  현재 HTTP 응답
     * @param handler   예외가 발생한 핸들러 객체
     * @param exception 발생한 예외
     * @return 예외 처리 결과 ModelAndView, 처리하지 못하면 {@code null}
     */
    ModelAndView resolveException(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception exception
    );
}
