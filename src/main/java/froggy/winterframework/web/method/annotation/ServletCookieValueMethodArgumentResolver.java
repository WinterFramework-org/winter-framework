package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.CookieValue;
import froggy.winterframework.web.context.request.NativeWebRequest;
import java.lang.reflect.Parameter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ServletCookieValueMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public ServletCookieValueMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(CookieValue.class);
    }

    @Override
    protected String extractValue(Parameter parameter, NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        Cookie[] cookies = servletRequest.getCookies();
        if (cookies == null) {
            return null;
        }

        String paramName = parameter.getAnnotation(CookieValue.class).value();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(paramName)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(Parameter parameter) {
        CookieValue ann = parameter.getAnnotation(CookieValue.class);
        return new NamedValueInfo(ann.value(), ann.required(), ann.defaultValue());
    }

}
