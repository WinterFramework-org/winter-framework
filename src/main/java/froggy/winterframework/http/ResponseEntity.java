package froggy.winterframework.http;

import java.net.URI;

/**
 * Status Code, Header, Body를 함께 가지는 HTTP Response 값.
 *
 * <p>immutable 객체로 설계되어 생성 후 상태가 변경되지 않는다.
 * {@link froggy.winterframework.stereotype.Controller @Controller} Handler와
 * {@link froggy.winterframework.web.bind.annotation.ExceptionHandler @ExceptionHandler}
 * 메서드의 반환값으로 사용되며, 클라이언트에게 세밀하게 제어된 HTTP Response를 전달할 때 유용하다.
 *
 * <p>정적 팩토리 메서드와 Builder를 통해 Response를 생성할 수 있다.
 */
public class ResponseEntity<T> {

    private final int status;
    private final HttpHeaders headers;
    private final T body;

    public ResponseEntity(T body, HttpStatus status) {
        this(body, new HttpHeaders(), status);
    }

    public ResponseEntity(T body, int status) {
        this(body, new HttpHeaders(), status);
    }

    public ResponseEntity(T body, HttpHeaders headers, HttpStatus status) {
        this(body, headers, requireStatus(status).value());
    }

    /**
     * Status Code, Header, Body를 복사해 immutable Response 값으로 구성한다.
     */
    public ResponseEntity(T body, HttpHeaders headers, int status) {
        validateStatus(status);
        this.status = status;
        this.headers = (headers != null ? new HttpHeaders(headers) : new HttpHeaders());
        this.body = body;
    }

    public int getStatusCode() {
        return this.status;
    }

    public HttpHeaders getHeaders() {
        return new HttpHeaders(this.headers);
    }

    public T getBody() {
        return this.body;
    }

    public static Builder status(HttpStatus status) {
        return new Builder(requireStatus(status).value());
    }

    public static Builder status(int status) {
        return new Builder(status);
    }

    public static Builder ok() {
        return status(HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> ok(T body) {
        return ok().body(body);
    }

    public static Builder created(URI location) {
        return status(HttpStatus.CREATED).header(HttpHeaders.LOCATION, location.toString());
    }

    public static Builder accepted() {
        return status(HttpStatus.ACCEPTED);
    }

    public static Builder noContent() {
        return status(HttpStatus.NO_CONTENT);
    }

    public static Builder badRequest() {
        return status(HttpStatus.BAD_REQUEST);
    }

    public static Builder unauthorized() {
        return status(HttpStatus.UNAUTHORIZED);
    }

    public static Builder forbidden() {
        return status(HttpStatus.FORBIDDEN);
    }

    public static Builder notFound() {
        return status(HttpStatus.NOT_FOUND);
    }

    public static Builder methodNotAllowed() {
        return status(HttpStatus.METHOD_NOT_ALLOWED);
    }

    public static Builder conflict() {
        return status(HttpStatus.CONFLICT);
    }

    public static Builder unsupportedMediaType() {
        return status(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    public static Builder unprocessableEntity() {
        return status(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static Builder tooManyRequests() {
        return status(HttpStatus.TOO_MANY_REQUESTS);
    }

    public static Builder internalServerError() {
        return status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static Builder serviceUnavailable() {
        return status(HttpStatus.SERVICE_UNAVAILABLE);
    }

    public static class Builder {

        private final int status;
        private final HttpHeaders headers = new HttpHeaders();

        private Builder(int status) {
            validateStatus(status);
            this.status = status;
        }

        public Builder header(String name, String... values) {
            if (values == null) {
                return this;
            }

            for (String value : values) {
                this.headers.add(name, value);
            }
            return this;
        }

        /**
         * Response Body의 Content-Type을 설정한다.
         */
        public Builder contentType(String contentType) {
            if (contentType == null || contentType.trim().isEmpty()) {
                throw new IllegalArgumentException("Content-Type must not be null or empty");
            }
            this.headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            return this;
        }

        public Builder headers(HttpHeaders headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * Body 없이 Response를 생성한다.
         */
        public ResponseEntity<Void> build() {
            return body(null);
        }

        /**
         * Body를 포함한 Response를 생성한다.
         */
        public <T> ResponseEntity<T> body(T body) {
            return new ResponseEntity<T>(body, this.headers, this.status);
        }
    }

    private static HttpStatus requireStatus(HttpStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("HttpStatus must not be null");
        }
        return status;
    }

    private static void validateStatus(int status) {
        if (status < 100 || status > 999) {
            throw new IllegalArgumentException("HTTP status must be a three-digit positive integer: " + status);
        }
    }
}
