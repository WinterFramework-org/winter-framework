package froggy.winterframework.web.method.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResponseBodyMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().isAnnotationPresent(ResponseBody.class) ||
            handlerMethod.getHandlerInstance().getClass().isAnnotationPresent(ResponseBody.class);
    }

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
