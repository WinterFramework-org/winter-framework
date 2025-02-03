package froggy.winterframework.beans.factory.support;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.utils.WinterUtils;
import java.lang.reflect.Constructor;
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
     * @throws RuntimeException beanName으로 등록된 BeanDefinition이 없는경우
     */
    public BeanDefinition getBeanDefinition(String beanName) throws RuntimeException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if(beanDefinition == null) {
            throw new IllegalStateException("No bean definition found for bean: " + beanName);
        }

        return beanDefinition;
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
     * @throws RuntimeException 이미 동일 이름의 BeanDefinition이 존재할 경우
     */
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws RuntimeException {
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
    public Object getBean(String beanName) throws RuntimeException {
        return doGetBean(beanName);
    }

    /**
     * Bean 이름으로 Bean을 조회/생성.
     * <p>구체적인 조회/생성 로직을 실행, {@link #getBean(String)}에 의해 호출되는 내부 메소드
     * 
     * @param beanName 조회할 Bean 이름
     * @return Bean 객체
     * @throws RuntimeException BeanDefinition이 존재하지 않는 경우, Bean 생성 중 예외 발생 시
     */
    protected Object doGetBean(String beanName) throws RuntimeException {
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
     * @throws RuntimeException Bean생성 중 발생한 예외
     */
    public Object createBean(String beanName, BeanDefinition beanDefinition) throws RuntimeException {
        return doCreateBean(beanName, beanDefinition);
    }

    /**
     * {@link BeanDefinition}를 참고하여 Bean 인스턴스를 생성하고 싱글톤 레지스트리에 등록.
     * <p>구체적인 생성 로직을 실행, {@link #createBean(String, BeanDefinition)}에 의해 호출.
     *
     * @param beanName       생성할 Bean 이름
     * @param beanDefinition BeanDefinition 정보
     * @return 생성된 Bean 객체
     * @throws RuntimeException Bean 생성 중 발생한 예외
     */
    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) throws RuntimeException {
        Class<?> beanClass = beanDefinition.getBeanClass();

        // @Autowired 선언된 생성자 찾기
        Constructor<?> autowiredConstructor = findAutowiredConstructor(beanClass);
        Object beanInstance = null;
        try {
            // @Autowired가 붙은 생성자가 있으면 의존성을 주입하여 인스턴스 생성
            if (autowiredConstructor != null) {
                Object[] parameters = resolveDependencies(autowiredConstructor);
                beanInstance = autowiredConstructor.newInstance(parameters);
            } else {
                // 기본 생성자로 인스턴스 생성
                beanInstance = beanClass.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to instantiate bean: " + beanName + " (class: " + beanClass.getName() + ")", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access while creating bean: " + beanName + " (class: " + beanClass.getName() + ")", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error invoking constructor for bean: " + beanName + " (class: " + beanClass.getName() + ")", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor found for bean: " + beanName + " (class: " + beanClass.getName() + ")", e);
        }

        registerSingleton(beanName, beanInstance);

        return beanInstance;
    }

    /**
     * 주어진 클래스에서 {@code @Autowired}가 붙은 생성자를 찾음.
     *
     * @param beanClass 대상 Bean 클래스
     * @return {@code @Autowired}가 붙은 생성자 (없으면 {@code null})
     */
    private Constructor<?> findAutowiredConstructor(Class<?> beanClass) {
        for (Constructor<?> constructor: beanClass.getDeclaredConstructors()) {
            if (WinterUtils.hasAnnotation(constructor, Autowired.class)) {
                return constructor;
            }
        }

        return null;
    }

    /**
     * DI를 위한 생성자 호출 시 필요한 Bean 인스턴스 배열을 반환.
     *
     * <ul>
     *     <li>생성자의 각 매개변수 타입을 조회하여 해당하는 Bean 객체를 가져옴</li>
     *     <li>가져온 Bean 객체들을 생성자 호출 시 인자로 전달할 배열로 반환</li>
     * </ul>
     * @param constructor DI가 필요한 생성자
     * @return 생성자 호출에 사용할 Bean 객체 배열
     */
    private Object[] resolveDependencies(Constructor<?> constructor) {
        Object[] parameters = new Object[constructor.getParameterCount()];

        int index = 0;
        for (Class<?> clazz: constructor.getParameterTypes()) {
            String dependencyBeanName = WinterUtils.resolveSimpleBeanName(clazz);
            try {
                parameters[index++] = getBean(dependencyBeanName);
            } catch (IllegalStateException e) {
                throw new IllegalStateException(
                    "Failed to resolve dependency: No qualifying bean of type '"
                        + clazz.getName() + "' found.", e);
            }

        }

        return parameters;
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