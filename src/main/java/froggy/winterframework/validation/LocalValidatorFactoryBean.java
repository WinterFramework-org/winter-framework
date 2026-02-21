package froggy.winterframework.validation;

import froggy.winterframework.beans.factory.InitializingBean;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.support.BeanFactory;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 * Hibernate Validator 팩토리를 초기화하고 WinterValidatorAdapter에 연결하는 진입점.
 */
public class LocalValidatorFactoryBean extends WinterValidatorAdapter implements InitializingBean {

    private final BeanFactory beanFactory;
    private ValidatorFactory validatorFactory;

    @Autowired
    public LocalValidatorFactoryBean(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        initializeValidatorFactory();
    }

    /**
     * Bean 초기화 시점에 ValidatorFactory를 부트스트랩한다.
     */
    @Override
    public synchronized void afterPropertiesSet() {
        initializeValidatorFactory();
    }

    /**
     * 현재 사용 중인 ValidatorFactory를 반환한다.
     *
     * @return ValidatorFactory
     */
    public synchronized ValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    private synchronized void initializeValidatorFactory() {
        if (this.validatorFactory != null) {
            return;
        }

        ValidatorFactory builtFactory = Validation.byDefaultProvider()
            .configure()
            .constraintValidatorFactory(new WinterConstraintValidatorFactory(beanFactory))
            .buildValidatorFactory();

        this.validatorFactory = builtFactory;
        setTargetValidator(builtFactory.getValidator());
    }
}
