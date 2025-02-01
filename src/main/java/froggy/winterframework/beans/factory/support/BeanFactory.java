package froggy.winterframework.beans.factory.support;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.utils.WinterUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean 생성, 등록, 조회를 담당하는 팩토리 클래스.
 *
 * {@link #getBean(String)}을 통해 Bean의 Life-cycle 관리
 * <p>현재 모든 Bean은 Singleton으로 관리.
 * 추후 Lazy Loading과 Prototype 지원.
 */
public class BeanFactory extends SingletonBeanRegistry {

    /** BeanName을 Key로 하는 BeanDefinition 객체 Map */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(32);

    /**
     * 지정한 이름의 {@link BeanDefinition}을 반환.
     *
     * @param beanName 조회할 Bean의 이름
     * @return {@link BeanDefinition}, 없으면 null
     */
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }

    /**
     * 등록된 {@link #beanDefinitionMap}를 반환.
     *
     * @return BeanDefinition Map
     */
    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return new HashMap<>(beanDefinitionMap);
    }

    /**
     * 등록된 모든 Bean 이름 목록을 반환.
     *
     * @return Bean 이름 List
     */
    public List<String> getBeanDefinitionNames() {
        return new ArrayList<>(beanDefinitionMap.keySet());
    }

    /**
     * 주어진 클래스에 대한 {@link BeanDefinition}을 등록.
     *
     * @param clazz Bean으로 등록할 클래스
     */
    public void registerBeanDefinition(Class<?> clazz) {
        registerBeanDefinition(
            WinterUtils.resolveSimpleBeanName(clazz),
            new BeanDefinition(clazz)
        );
    }

    /**
     * 지정한 이름과 {@link BeanDefinition}으로 Bean을 등록.
     *
     * @param beanName       Bean 이름
     * @param beanDefinition 등록할 BeanDefinition
     * @throws IllegalStateException 이미 동일 이름의 BeanDefinition이 존재할 경우
     */
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        BeanDefinition bd = beanDefinitionMap.putIfAbsent(beanName, beanDefinition);

        if (bd != null) {
            throw new IllegalStateException("The bean definition already exists and cannot be overridden.");
        }
    }

    /**
     * 등록된 Bean을 조회하여 반환하는 외부 메소드.
     *
     * <p>Singleton Bean이 존재하면 반환하고, 없으면 새로 생성
     *
     * @param beanName 조회할 Bean 이름
     * @return Bean 객체
     */
    public Object getBean(String beanName) {
        return doGetBean(beanName);
    }

    /**
     * Bean 이름으로 Bean을 조회/생성.
     * <p>구체적인 조회/생성 로직을 실행, {@link #getBean(String)}에 의해 호출되는 내부 메소드
     * 
     * @param beanName 조회할 Bean 이름
     * @return Bean 객체
     */
    protected Object doGetBean(String beanName) {
        Object beanInstance = getSingleton(beanName);

        if (beanInstance != null) {
            return beanInstance;
        }

        // 존재하지 않으면 빈 정의 정보를 기반으로 빈 생성
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        return createBean(beanName, beanDefinition);
    }

    /**
     * {@link BeanDefinition}을 기반으로 Bean을 생성.
     *
     * @param beanName       생성할 Bean 이름
     * @param beanDefinition BeanDefinition 정보
     * @return 생성된 Bean 객체
     */
    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        return doCreateBean(beanName, beanDefinition);
    }

    /**
     * {@link BeanDefinition}를 참고하여 Bean 인스턴스를 생성하고 싱글톤 레지스트리에 등록.
     * <p>구체적인 생성 로직을 실행, {@link #createBean(String, BeanDefinition)}에 의해 호출.
     *
     * @param beanName       생성할 Bean 이름
     * @param beanDefinition BeanDefinition 정보
     * @return 생성된 Bean 객체
     * @throws RuntimeException 생성 중 발생한 예외
     */
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

    /**
     * 미리 등록된 모든 Bean의 싱글톤 인스턴스를 생성.
     */
    public void preInstantiateSingletons() {
        for (String beanName : getBeanDefinitionNames()) {
            getBean(beanName);
        }
    }

    /**
     * 지정한 이름의 {@link BeanDefinition}이 존재하는지 확인.
     *
     * @param beanName 확인할 Bean 이름
     * @return 존재하면 {@code true}, 그렇지 않으면 {@code false}
     */
    protected boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

}