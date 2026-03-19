package froggy.winterframework.web.servlet.handler;

import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.servlet.ExceptionResolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 여러 {@link ExceptionResolver}를 체인 형태로 구성하여 예외 처리를 위임하는 Composite 클래스.
 *
 * <p>등록된 Resolver들을 순차적으로 실행하며, 가장 먼저 반환된 결과를 최종 응답으로 처리한다.
 */
public class HandlerExceptionResolverComposite implements ExceptionResolver {

    private List<ExceptionResolver> resolvers = new ArrayList<>();

    public void setExceptionResolvers(List<ExceptionResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public List<ExceptionResolver> getExceptionResolvers() {
        return Collections.unmodifiableList(this.resolvers);
    }

    @Override
    public ModelAndView resolveException(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex
    ) {
        for (ExceptionResolver resolver : this.resolvers) {
            try {
                ModelAndView mav = resolver.resolveException(request, response, handler, ex);
                if (mav != null) {
                    return mav;
                }
            } catch (Exception resolveEx) {
                // 예외 처리가 실패하면 다음 resolver로 넘긴다.
            }
        }
        return null;
    }
}
