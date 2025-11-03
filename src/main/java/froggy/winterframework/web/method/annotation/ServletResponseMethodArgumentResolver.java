package froggy.winterframework.web.method.annotation;

import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import java.lang.reflect.Parameter;
import javax.servlet.ServletResponse;

public class ServletResponseMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return ServletResponse.class.isAssignableFrom(parameter.getType());
    }

    /**
     * Handler Method의 파라미터로 선언된 {@link ServletResponse} 타입을 처리하며,
     * Response 객체를 바인딩한다.
     *
     * <p>
     * 해당 Method가 반환값을 통해 View Rendering 과정을 거치지 않고, 응답을 직접 제어할 수 있음을 나타내기 위해
     * {@link ModelAndView#setRequestHandled(boolean)}을 {@code true}로 설정한다.
     * 이후 Handler Method가 {@code null}을 반환하는 경우,
     * 요청을 직접 처리한 것으로 간주한다.
     */
    @Override
    public Object resolveArgument(Parameter parameter, NativeWebRequest webRequest,
        ModelAndView mavContainer) throws Exception {

        mavContainer.setRequestHandled(true);

        ServletResponse response = webRequest.getNativeResponse(ServletResponse.class);
        if (response == null) {
            throw new IllegalStateException(
                "No native ServletResponse available for parameter: " + parameter.getName()
            );
        }
        return response;
    }

}
