package froggy.winterframework.web.method.annotation;

import froggy.winterframework.core.MethodParameter;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import javax.servlet.ServletRequest;

public class ServletRequestMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return NativeWebRequest.class.isAssignableFrom(parameterType)
            || ServletRequest.class.isAssignableFrom(parameterType);
    }

    /**
     * Handler Method 파라미터의 {@link NativeWebRequest} 또는
     * {@link ServletRequest} 계열 타입을 해석한다.
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, NativeWebRequest webRequest,
        ModelAndView mavContainer) throws Exception {

        Class<?> parameterType = parameter.getParameterType();
        if (NativeWebRequest.class.isAssignableFrom(parameterType)) {
            return webRequest;
        }

        Object request = webRequest.getNativeRequest(parameterType);
        if (request == null) {
            throw new IllegalStateException(
                "No native request available for parameter: " + parameter.getParameterName()
            );
        }

        return request;
    }

}
