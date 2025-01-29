package froggy.winterframework.context.support;

import froggy.winterframework.context.ApplicationContext;

public class ApplicationContextSupport {

    private final ApplicationContext applicationContext;

    public ApplicationContextSupport(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
