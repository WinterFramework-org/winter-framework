package froggy.winterframework.web.servlet.mvc.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import froggy.winterframework.validation.MethodArgumentNotValidException;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.servlet.ExceptionResolver;
import froggy.winterframework.web.servlet.MethodNotAllowedException;
import froggy.winterframework.web.servlet.NoHandlerFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 핸들러 실행 중 발생한 기본 웹 예외를 처리하는 Resolver.
 *
 * <p>예외를 적절한 HTTP 오류 응답으로 변환한다.
 */
public class DefaultHandlerExceptionResolver implements ExceptionResolver {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * 기본 예외를 HTTP 상태 코드에 맞는 응답으로 변환한다.
     *
     * @param request   현재 HTTP 요청
     * @param response  현재 HTTP 응답
     * @param handler   예외가 발생한 핸들러 객체
     * @param exception 발생한 예외
     * @return 처리 결과 ModelAndView, 처리 대상이 아니면 {@code null}
     */
    @Override
    public ModelAndView resolveException(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception exception
    ) {
        ResolvedError resolvedError = resolveError(exception);
        // 처리 대상이 아니면 다음 ExceptionResolver로 넘긴다.
        if (resolvedError == null) {
            return null;
        }

        try {
            writeResolvedError(request, response, resolvedError);
        } catch (Exception ex) {
            // 예외 처리가 실패하면 다음 resolver로 넘긴다.
            return null;
        }
        return handledModelAndView();
    }

    private ResolvedError resolveError(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException) {
            return resolveValidationError();
        }

        if (exception instanceof NoHandlerFoundException) {
            return resolveNoHandlerFoundError(exception);
        }

        if (exception instanceof MethodNotAllowedException) {
            return resolveMethodNotAllowedError((MethodNotAllowedException) exception);
        }

        return null;
    }

    private ResolvedError resolveValidationError() {
        return ResolvedError.of(
            HttpServletResponse.SC_BAD_REQUEST,
            "BAD_REQUEST",
            "Validation failed"
        );
    }

    private ResolvedError resolveNoHandlerFoundError(Exception exception) {
        return ResolvedError.of(
            HttpServletResponse.SC_NOT_FOUND,
            "NOT_FOUND",
            nonEmptyMessage(exception.getMessage(), "No handler found")
        );
    }

    private ResolvedError resolveMethodNotAllowedError(MethodNotAllowedException methodException) {
        Map<String, String> headers = new LinkedHashMap<>();
        // 405 응답에는 허용 메서드를 Allow 헤더로 함께 내려준다.
        headers.put("Allow", methodException.getAllowHeader());

        return ResolvedError.of(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "METHOD_NOT_ALLOWED",
            nonEmptyMessage(methodException.getMessage(), "Method not allowed"),
            headers
        );
    }

    private void writeResolvedError(
        HttpServletRequest request,
        HttpServletResponse response,
        ResolvedError resolvedError
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        applyHeaders(response, resolvedError);

        if (isJsonPreferred(request)) {
            // JSON 요청이면 오류 정보를 JSON 응답으로 작성한다.
            writeJsonResponse(request, response, resolvedError);
            return;
        }

        // 그 외 요청은 서블릿 컨테이너의 기본 오류 처리로 넘긴다.
        response.sendError(resolvedError.getStatus(), resolvedError.getMessage());
    }

    private void applyHeaders(HttpServletResponse response, ResolvedError resolvedError) {
        for (Map.Entry<String, String> header : resolvedError.getHeaders().entrySet()) {
            response.setHeader(header.getKey(), header.getValue());
        }
    }

    private void writeJsonResponse(
        HttpServletRequest request,
        HttpServletResponse response,
        ResolvedError resolvedError
    ) throws IOException {
        response.setStatus(resolvedError.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getOutputStream(), createJsonBody(request, resolvedError));
    }

    private Map<String, Object> createJsonBody(
        HttpServletRequest request,
        ResolvedError resolvedError
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", resolvedError.getStatus());
        body.put("code", resolvedError.getCode());
        body.put("message", resolvedError.getMessage());
        body.put("path", request.getRequestURI() != null ? request.getRequestURI() : "");
        return body;
    }

    private boolean isJsonPreferred(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        if (accept == null) {
            return false;
        }

        String lowerAccept = accept.toLowerCase(Locale.ROOT);
        return lowerAccept.contains("application/json") || lowerAccept.contains("+json");
    }

    private String nonEmptyMessage(String message, String fallback) {
        if (message == null || message.trim().isEmpty()) {
            return fallback;
        }
        return message;
    }

    private ModelAndView handledModelAndView() {
        ModelAndView modelAndView = ModelAndView.createContainer();
        // 응답 처리로 표시한다.
        modelAndView.setRequestHandled(true);
        return modelAndView;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private static final class ResolvedError {

        private final int status;
        private final String code;
        private final String message;
        private final Map<String, String> headers;

        private ResolvedError(int status, String code, String message, Map<String, String> headers) {
            this.status = status;
            this.code = code;
            this.message = message;
            this.headers = new LinkedHashMap<>(headers);
        }

        private static ResolvedError of(int status, String code, String message) {
            return new ResolvedError(status, code, message, Collections.emptyMap());
        }

        private static ResolvedError of(
            int status,
            String code,
            String message,
            Map<String, String> headers
        ) {
            return new ResolvedError(status, code, message, headers);
        }

        private int getStatus() {
            return status;
        }

        private String getCode() {
            return code;
        }

        private String getMessage() {
            return message;
        }

        private Map<String, String> getHeaders() {
            return headers;
        }
    }
}
