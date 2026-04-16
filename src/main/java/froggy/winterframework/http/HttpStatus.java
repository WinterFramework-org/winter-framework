package froggy.winterframework.http;

/**
 * HTTP 상태 코드를 정의한 Enum.
 */
public enum HttpStatus {

    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    TEMPORARY_REDIRECT(307),
    PERMANENT_REDIRECT(308),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    CONFLICT(409),
    UNSUPPORTED_MEDIA_TYPE(415),
    UNPROCESSABLE_ENTITY(422),
    TOO_MANY_REQUESTS(429),
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503);

    private final int value;

    HttpStatus(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
