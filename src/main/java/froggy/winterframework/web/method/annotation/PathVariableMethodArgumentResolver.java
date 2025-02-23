package froggy.winterframework.web.method.annotation;

import froggy.winterframework.utils.convert.TypeConverter;
import froggy.winterframework.web.bind.annotation.PathVariable;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

public class PathVariableMethodArgumentResolver extends AbstractMethodArgumentResolver {

    public PathVariableMethodArgumentResolver(TypeConverter converter) {
        super(converter);
    }

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(PathVariable.class);
    }

    @Override
    protected String extractValue(Parameter parameter, HttpServletRequest request) {
        String paramName = parameter.getAnnotation(PathVariable.class).value();
        HashMap<String, String> map = (HashMap<String, String>) request.getAttribute("uriTemplateVariables");

        return map.get(paramName);
    }
}
