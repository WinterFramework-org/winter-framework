package froggy.winterframework.web.method.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import froggy.winterframework.http.HttpHeaders;
import froggy.winterframework.http.ResponseEntity;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.method.HandlerMethod;
import froggy.winterframework.web.method.support.HandlerMethodReturnValueHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 * ResponseEntity 반환값을 Servlet Response로 Write하는 ReturnValueHandler.
 *
 * <p>Status Code, Header, Body를 Response에 직접 Write한다.
 */
public class ResponseEntityMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String TEXT_PLAIN = "text/plain";

    private final ObjectMapper objectMapper = createObjectMapper();

    @Override
    public boolean supportsReturnType(HandlerMethod handlerMethod) {
        return ResponseEntity.class.isAssignableFrom(handlerMethod.getReturnType());
    }

    @Override
    public void handleReturnValue(
        Object returnValue,
        Class<?> returnType,
        NativeWebRequest webRequest,
        ModelAndView mavContainer
    ) {
        mavContainer.setRequestHandled(true);

        if (returnValue == null) {
            return;
        }

        ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnValue;
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        response.setStatus(responseEntity.getStatusCode());

        applyHeaders(response, responseEntity);

        Object body = responseEntity.getBody();
        if (body == null) {
            return;
        }

        try {
            if (response.getContentType() == null) {
                applyDefaultContentType(response, body);
            }
            applyDefaultCharacterEncoding(response, body);

            writeBody(response, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ResponseEntity에 담긴 Header를 Servlet Response에 반영한다.
     * Content-Type은 단일 값으로 처리한다.
     */
    private void applyHeaders(HttpServletResponse response, ResponseEntity<?> responseEntity) {
        Map<String, List<String>> headers = responseEntity.getHeaders().toMap();
        List<String> contentTypes = headers.get(HttpHeaders.CONTENT_TYPE);
        if (contentTypes != null && !contentTypes.isEmpty()) {
            response.setContentType(contentTypes.get(0));
        }

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (HttpHeaders.CONTENT_TYPE.equals(entry.getKey())) {
                continue;
            }
            for (String value : entry.getValue()) {
                response.addHeader(entry.getKey(), value);
            }
        }
    }

    /**
     * Content-Type이 지정되지 않은 경우 Body 타입 기준 기본값을 정한다.
     */
    private void applyDefaultContentType(HttpServletResponse response, Object body) {
        if (body instanceof String) {
            response.setContentType(TEXT_PLAIN);
            return;
        }

        if (body instanceof byte[]) {
            response.setContentType(APPLICATION_OCTET_STREAM);
            return;
        }

        response.setContentType(APPLICATION_JSON);
    }

    /**
     * CharacterEncoding을 설정한다.
     */
    private void applyDefaultCharacterEncoding(HttpServletResponse response, Object body) {
        if (body instanceof byte[]) {
            return;
        }

        if (response.getCharacterEncoding() != null) {
            return;
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    }

    /**
     * Body를 Response에 Write한다.
     */
    private void writeBody(HttpServletResponse response, Object body) throws IOException {
        if (body instanceof String) {
            String text = (String) body;
            response.getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return;
        }

        if (body instanceof byte[]) {
            byte[] bytes = (byte[]) body;
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            return;
        }

        objectMapper.writeValue(response.getOutputStream(), body);
        response.getOutputStream().flush();
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
