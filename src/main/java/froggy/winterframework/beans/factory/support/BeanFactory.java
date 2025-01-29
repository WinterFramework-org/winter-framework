package froggy.winterframework.beans.factory.support;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.utils.WinterUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory extends SingletonBeanRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(32);

    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }

    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return new HashMap<>(beanDefinitionMap);
    }

    public List<String> getBeanDefinitionNames() {
        return new ArrayList<>(beanDefinitionMap.keySet());
    }

    public void registerBeanDefinition(Class<?> clazz) {
        registerBeanDefinition(
            WinterUtils.resolveSimpleBeanName(clazz),
            new BeanDefinition(clazz)
        );
    }

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        BeanDefinition bd = beanDefinitionMap.putIfAbsent(beanName, beanDefinition);

        if (bd != null) {
            throw new IllegalStateException("The bean definition already exists and cannot be overridden.");
        }
    }

    public Object getBean(String beanName) {
        return doGetBean(beanName);
    }

    protected Object doGetBean(String beanName) {
        Object beanInstance = getSingleton(beanName);

        if (beanInstance != null) {
            return beanInstance;
        }

        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        return createBean(beanName, beanDefinition);
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        return doCreateBean(beanName, beanDefinition);
    }

    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();

        Object beanInstance = null;
        try {
            beanInstance = beanClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to instantiate bean: " + beanName, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access while creating bean: " + beanName, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error invoking constructor for bean: " + beanName, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor found for bean: " + beanName, e);
        }

        registerSingleton(beanName, beanInstance);

        return beanInstance;
    }

    public void preInstantiateSingletons() {
        for (String beanName : getBeanDefinitionNames()) {
            getBean(beanName);
        }
    }

    protected boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

}