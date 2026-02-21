package froggy.winterframework.validation;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.beans.factory.config.ScopeType;
import froggy.winterframework.beans.factory.support.BeanFactory;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

/**
 * ConstraintValidator 생성 시 Winter DI를 연결하는 팩토리 구현체.
 *
 * <p>BeanFactory가 존재하면 Winter DI를 적용해 Validator를 생성한다.
 * BeanFactory가 없으면 기본 생성자로 직접 인스턴스를 만든다.</p>
 */
public class WinterConstraintValidatorFactory implements ConstraintValidatorFactory {

    private static final String VALIDATOR_BEAN_PREFIX = "__winter_constraint_validator__";

    private final BeanFactory beanFactory;

    public WinterConstraintValidatorFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 지정된 타입의 {@link ConstraintValidator} 인스턴스를 반환한다.
     *
     * <p>
     * 현재 구현은 Validator를 prototype으로 관리한다는 전제에서,
     * 매 요청마다 임시 Bean 이름으로 DI 생성한다.
     *
     * @param targetClass ConstraintValidator의 구현 클래스
     * @param <T> ConstraintValidator의 타입
     * @return ConstraintValidator 인스턴스
     */
    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("ConstraintValidator type must not be null");
        }

        Object validatorInstance = beanFactory.createBean(
            VALIDATOR_BEAN_PREFIX + targetClass.getName(),
            new BeanDefinition(targetClass, ScopeType.PROTOTYPE)
        );
        return targetClass.cast(validatorInstance);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        // 현재는 별도 정리 작업을 수행하지 않고 GC에 위임한다.
    }

}
