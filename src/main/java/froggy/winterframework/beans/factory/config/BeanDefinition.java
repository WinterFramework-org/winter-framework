package froggy.winterframework.beans.factory.config;

public class BeanDefinition {

    private Class<?> beanClass;
    private String scope;

    public BeanDefinition(Class<?> beanClass, String scope) {
        this.beanClass = beanClass;
        this.scope = scope;
    }

    public BeanDefinition(Class<?> beanClass) {
        this(beanClass, "singleton");
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getScope() {
        return scope;
    }

}
