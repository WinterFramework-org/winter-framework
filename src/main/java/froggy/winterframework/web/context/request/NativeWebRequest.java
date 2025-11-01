package froggy.winterframework.web.context.request;

/**
 * 요청 및 세션 범위의 속성 접근과 요청 메타데이터를 다루기 위한 인터페이스.
 * <p>
 * 실제 요청 처리용이 아니라, 요청·응답 객체나 속성에 접근할 수 있도록
 * 프레임워크 내부(예: ArgumentResolver 등)에서 사용된다.
 */

/**
 * 프레임워크 내부용 인터페이스.
 * <p>
 * Request·Response 객체와 Header, Parameter 및
 * 요청/세션 Attribute에 대한 추상화된 접근 방법을 제공
 */
public interface NativeWebRequest {

    final int SCOPE_REQUEST = 0;

    final int SCOPE_SESSION = 1;

    <T> T getNativeRequest(Class<T> requiredType);
    <T> T getNativeResponse(Class<T> requiredType);

    String getHeader(String headerName);
    String getParameter(String paramName);
    Object getAttribute(String name, int scope);
    void setAttribute(String name, Object value, int scope);
}
