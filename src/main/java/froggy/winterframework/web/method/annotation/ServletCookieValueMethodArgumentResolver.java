package froggy.winterframework.web.method.annotation;

import froggy.winterframework.core.MethodParameter;
import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.CookieValue;
import froggy.winterframework.web.context.request.NativeWebRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ServletCookieValueMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public ServletCookieValueMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CookieValue.class);
    }

    @Override
    protected String extractValue(MethodParameter parameter, NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        Cookie[] cookies = servletRequest.getCookies();
        if (cookies == null) {
            return null;
        }

        String paramName = parameter.getParameterAnnotation(CookieValue.class).value();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(paramName)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        CookieValue ann = parameter.getParameterAnnotation(CookieValue.class);
        return new NamedValueInfo(ann.value(), ann.required(), ann.defaultValue());
    }

}
