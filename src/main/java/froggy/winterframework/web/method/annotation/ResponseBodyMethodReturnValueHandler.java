package froggy.winterframework.web.method.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler 메서드에 {@link ResponseBody} 어노테이션이 적용된 경우
 * 해당 메서드의 반환 값을 JSON 형식으로 변환하여 HttpResponse의 Body에 저장.
 */
public class ResponseBodyMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    /**
     * Method 혹은 Handler 클래스에 @ResponseBody 어노테이션이 적용되어있는지 확인
     *
     * @param handlerMethod 처리 대상이 되는 핸들러 메서드 정보를 포함하는 객체
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supportsReturnType(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().isAnnotationPresent(ResponseBody.class) ||
            handlerMethod.getHandlerInstance().getClass().isAnnotationPresent(ResponseBody.class);
    }

    /**
     * 컨트롤러 메서드가 반환한 값을 JSON으로 변환하여 HttpServletResponse에 저장
     *
     * @param returnValue 핸들러 메소드의 반환 값
     * @param returnType 반환 값의 클래스 타입
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     */
    @Override
    public void handleReturnValue(Object returnValue, Class<?> returnType,
        HttpServletRequest request, HttpServletResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ServletOutputStream outputMessage = response.getOutputStream();
            objectMapper.writeValue(outputMessage, returnValue);
            outputMessage.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
