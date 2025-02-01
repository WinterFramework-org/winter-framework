package froggy.winterframework.context.support;

import froggy.winterframework.context.ApplicationContext;

/**
 * {@link ApplicationContext}를 쉽게 참조 할 수 있도록 제공하는 클래스
 *
 * <p>프레임워크 내부에서 {@code ApplicationContext}를 공유하기 위해 사용</p>
 */
public class ApplicationContextSupport {

    private final ApplicationContext applicationContext;

    public ApplicationContextSupport(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
