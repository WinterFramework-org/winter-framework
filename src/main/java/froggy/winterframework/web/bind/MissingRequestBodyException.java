package froggy.winterframework.web.bind;

/**
 * 요청 본문 값이 없을 때 컨트롤러 호출 전에 발생하는 예외.
 *
 * <p>{@code required=true}이면 본문 값이 없을 때 발생한다.
 * {@code required=false}여도 기본형 파라미터는 null을 받을 수 없으므로,
 * 본문 값이 없으면 발생한다.
 * 기본 ExceptionResolver가 HTTP 400으로 처리한다.
 */
public class MissingRequestBodyException extends RuntimeException {

    /**
     * 컨트롤러에 전달할 요청 본문 객체가 없음을 나타낸다.
     */
    public MissingRequestBodyException() {
        super("Request body must not be empty or null");
    }
}
