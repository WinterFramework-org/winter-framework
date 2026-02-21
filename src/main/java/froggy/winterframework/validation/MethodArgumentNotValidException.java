package froggy.winterframework.validation;

import froggy.winterframework.core.MethodParameter;

/**
 * 컨트롤러 파라미터 검증 실패 시 발생하는 예외.
 *
 * <p>다음 파라미터가 {@link BindingResult}가 아닌 경우 즉시 발생한다.</p>
 */
public class MethodArgumentNotValidException extends RuntimeException {

    private final MethodParameter methodParameter;
    private final BindingResult bindingResult;

    public MethodArgumentNotValidException(MethodParameter methodParameter, BindingResult bindingResult) {
        super(buildMessage(methodParameter, bindingResult));
        this.methodParameter = methodParameter;
        this.bindingResult = bindingResult;
    }

    public MethodParameter getMethodParameter() {
        return methodParameter;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }

    private static String buildMessage(MethodParameter parameter, BindingResult bindingResult) {
        return "Validation failed for argument at index [" + parameter.getParameterIndex() + "] in method ["
            + parameter.getMethod().getDeclaringClass().getSimpleName() + "#"
            + parameter.getMethod().getName() + "], errorCount=" + bindingResult.getErrorCount();
    }
}
