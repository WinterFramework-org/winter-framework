package froggy.winterframework.context;

import froggy.winterframework.beans.factory.support.BeanFactory;

public class ApplicationContext {

    private final BeanFactory beanFactory;

    public ApplicationContext() {
        beanFactory = new BeanFactory();
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Object getBean(String beanName) {
        return getBeanFactory().getBean(beanName);
    }

}
