package froggy.winterframework.beans.factory;

public interface InitializingBean {

    void afterPropertiesSet() throws Exception;

}
