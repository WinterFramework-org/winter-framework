package froggy.winterframework.beans.factory.support;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton Bean을 저장하고 관리하는 레지스트리.
 *
 * <p>`BeanFactory`에서 생성된 싱글톤 Bean을 저장하며,
 * 이미 등록된 Bean이 있을 경우 중복 등록을 방지.
 */

public class SingletonBeanRegistry {

    /** BeanName을 Key로 하는 Bean 객체 Map */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(32);

    /**
     * Singleton Bean을 저장소에 등록.
     *
     * @param beanName        등록할 Bean 이름
     * @param singletonObject 등록할 Bean 객체
     * @throws IllegalStateException 동일 이름의 Bean이 이미 등록된 경우
     */
    public void registerSingleton(String beanName, Object singletonObject) {
        addSingleton(beanName, singletonObject);
    }

    /**
     * Singleton Bean을 {@link #singletonObjects}에 등록.
     * <p>구체적인 저장 로직을 실행하며, {@link #registerSingleton(String, Object)}에 의해 호출.</p>
     *
     * @param beanName        등록할 Bean 이름
     * @param singletonObject 등록할 Bean 객체
     * @throws IllegalStateException 동일 이름의 Bean이 이미 존재하는 경우
     */
    private void addSingleton(String beanName, Object singletonObject) {
        Object oldObject = singletonObjects.putIfAbsent(beanName, singletonObject);

        if (oldObject != null) {
            throw new IllegalStateException("A Bean with the name '" + beanName + "' is already registered.");
        }
    }

    /**
     * 저장된 Singleton Bean 조회.
     *
     * @param beanName 조회할 Bean의 이름
     * @return Singleton Bean 객체, 없으면 null
     */
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    /**
     * 저장된 Singleton BeanName 리스트를 반환
     *
     * @return Singleton BeanName 리스트
     */
    protected ArrayList<String> getSingletonNames() {
        return new ArrayList<>(singletonObjects.keySet());
    }

}
