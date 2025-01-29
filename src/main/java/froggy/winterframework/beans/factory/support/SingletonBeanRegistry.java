package froggy.winterframework.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonBeanRegistry {

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(32);

    public void registerSingleton(String beanName, Object singletonObject) {
        addSingleton(beanName, singletonObject);
    }

    private void addSingleton(String beanName, Object singletonObject) {
        Object oldObject = singletonObjects.putIfAbsent(beanName, singletonObject);

        if (oldObject != null) {
            throw new IllegalStateException("A Bean with the name '" + beanName + "' is already registered.");
        }
    }

    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

}
