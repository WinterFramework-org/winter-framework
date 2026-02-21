package froggy.winterframework.validation;

import java.util.Set;
import javax.validation.ConstraintViolation;

/**
 * Winter 검증 인터페이스를 Bean Validation(javax.validation) 엔진에 연결하는 어댑터.
 */
public class WinterValidatorAdapter implements Validator {

    private javax.validation.Validator targetValidator;

    public WinterValidatorAdapter() {
    }

    public WinterValidatorAdapter(javax.validation.Validator targetValidator) {
        setTargetValidator(targetValidator);
    }

    @Override
    public boolean supports(Class<?> targetClass) {
        return targetClass != null;
    }

    @Override
    public void validate(Object target, BindingResult bindingResult, Class<?>... groups) {
        if (target == null) {
            throw new IllegalArgumentException("Validation target must not be null");
        }

        if (bindingResult == null) {
            throw new IllegalArgumentException("BindingResult must not be null");
        }

        if (!supports(target.getClass())) {
            throw new IllegalArgumentException("Unsupported target type: " + target.getClass().getName());
        }

        Class<?>[] validateGroups = groups != null ? groups : new Class<?>[0];

        Set<ConstraintViolation<Object>> violations =
            getTargetValidator().validate(target, validateGroups);

        for (ConstraintViolation<Object> violation : violations) {
            String propertyPath = violation.getPropertyPath() != null
                ? violation.getPropertyPath().toString()
                : "";

            if (propertyPath.isEmpty()) {
                bindingResult.addGlobalError(violation.getMessageTemplate(), violation.getMessage());
                continue;
            }

            bindingResult.addFieldError(
                propertyPath,
                violation.getInvalidValue(),
                violation.getMessageTemplate(),
                violation.getMessage()
            );
        }
    }

    /**
     * 내부 Bean Validation 검증기를 교체한다.
     *
     * @param targetValidator 실제 검증 엔진
     */
    protected final void setTargetValidator(javax.validation.Validator targetValidator) {
        if (targetValidator == null) {
            throw new IllegalArgumentException("Target validator must not be null");
        }
        this.targetValidator = targetValidator;
    }

    /**
     * 내부 Bean Validation 검증기를 반환한다.
     *
     * @return 실제 검증 엔진
     */
    protected javax.validation.Validator getTargetValidator() {
        if (targetValidator == null) {
            throw new IllegalStateException("Target validator is not initialized");
        }
        return targetValidator;
    }
}
