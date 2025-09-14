package froggy.winterframework.web.method.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import froggy.winterframework.web.bind.annotation.RequestBody;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Parameter;
import javax.servlet.http.HttpServletRequest;

/**
 * @RequestBody 애노테이션이 붙은 파라미터를 처리하는 Argument Resolver.
 *
 * <p>HTTP 요청 본문(JSON)을 읽어 Java 객체로 변환해 컨트롤러 메서드 파라미터로 주입.</p>
 */
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Parameter에 @RequestBody 어노테이션이 적용되어있는지 확인
     *
     * @param parameter 컨트롤러 메서드의 파라미터 정보
     * @return 지원하면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestBody.class);
    }

    /**
     * HTTP 요청 본문을 읽어 파라미터 타입에 맞는 객체로 변환.
     *
     * @param parameter @RequestBody가 붙은 메서드 파라미터
     * @param request   현재 HTTP 요청 객체
     * @return 변환된 객체 (요청 본문을 파싱한 결과)
     * @throws Exception 변환 중 오류 발생 시
     */
    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read request body.", e);
        }

        String requestData = sb.toString();
        return parseJsonToType(requestData, parameter.getType());
    }

    private <T> T parseJsonToType(String requestData, Class<T> requiredType) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(requestData, requiredType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                "Invalid JSON request body. An error occurred during parsing: \n" + e.getMessage(), e
            );
        }
    }

}
