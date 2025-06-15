package froggy.winterframework.beans.factory.support;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.annotation.Value;
import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.core.env.Environment;
import froggy.winterframework.utils.WinterUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /** 애플리케이션 전체 설정(properties, 환경변수 등)을 제공하는 Environment */
    private Environment environment;

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
     * Environment 객체를 받아 내부 필드에 설정함
     *
     * @param environment 애플리케이션 전체 설정(properties, 환경변수 등)을 제공
     */
    public void addEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 주어진 클래스 타입으로 등록된 Bean 이름들을 반환
     *
     * @param requiredType 대상 클래스
     * @return 해당 타입의 Bean 이름 목록
     */
    public List<String> getBeanNamesForType(Class<?> requiredType) {
        List<String> matchedNames = new ArrayList<>();

        Set<String> candidates = new HashSet<>(getBeanDefinitionNames());
        candidates.addAll(getSingletonNames());

        for (String beanName : candidates) {
            Class<?> beanType = null;

            // BeanDefinition에 등록된 Bean 조회
            if (containsBeanDefinition(beanName)) {
                beanType = getBeanDefinition(beanName).getBeanClass();
            }
            // BeanDefinition에 없는 Singleton Bean 조회 (registerSingleton()으로만 등록된 경우)
            else {
                Object singleton = getSingleton(beanName);
                if (singleton != null) {
                    beanType = singleton.getClass();
                }
            }

            if (beanType != null && requiredType.isAssignableFrom(beanType)) {
                matchedNames.add(beanName);
            }
        }

        return matchedNames;
    }

    /**
     * 주어진 클래스에 대한 {@link BeanDefinition}을 등록
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
     * 등록된 Bean을 조회하여 반환하는 외부 메소드.
     *
     * <p>Singleton Bean이 존재하면 반환하고, 없으면 새로 생성
     *
     * @param beanName          조회할 Bean의 이름
     * @param requiredType      반환할 Bean의 타입
     * @return                  조회된 Bean 객체를 지정된 타입으로 캐스팅하여 반환
     * @throws RuntimeException 반환된 Bean이 요구하는 타입이 아닐 경우 발생
     */
    public <T> T getBean(String beanName, Class<T> requiredType) throws RuntimeException {
        Object bean = doGetBean(beanName);

        if (requiredType != null && !requiredType.isInstance(bean)) {
            throw new RuntimeException("Bean named '" + beanName + "' is not of required type: " + requiredType.getName());
        }

        return requiredType != null ? requiredType.cast(bean) : (T) bean;
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
        Object beanInstance = createBeanInstance(beanName, beanDefinition);

        registerSingleton(beanName, beanInstance);

        return beanInstance;
    }

    /**
     * BeanDefinition 체크하여 FactoryMethod 또는 생성자를 통해 인스턴스를 생성.
     *
     * @param beanName       생성할 Bean 이름
     * @param beanDefinition BeanDefinition 정보
     * @return 생성된 Bean 객체
     */
    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition) {
        if (beanDefinition.getFactoryMethodName() != null) {
            return instantiateUsingFactoryMethod(beanName, beanDefinition);
        }

        return autowireConstructor(beanName, beanDefinition);
    }

    /**
     * FactoryMethod를 통해 Bean으로 등록할 인스턴스를 생성.
     *
     * @param beanName       생성할 Bean 이름
     * @param beanDefinition Bean 정의 정보
     * @return 생성된 Bean 객체
     */
    private Object instantiateUsingFactoryMethod(String beanName, BeanDefinition beanDefinition) {
        String factoryBeanName = beanDefinition.getFactoryBeanName();
        Object factoryBean = getBean(factoryBeanName);

        String methodName = beanDefinition.getFactoryMethodName();
        try {
            Method factoryMethod = factoryBean.getClass().getMethod(methodName);

            return factoryMethod.invoke(factoryBean);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Factory method not found: " + methodName + " for bean '" + beanName + "'", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke factory method: " + methodName + " on bean '" + beanName + "'", e);
        }
    }

    /**
     * 생성자에 @Autowired가 있으면 의존성을 주입하고, 없으면 기본 생성자로 인스턴스 생성.
     *
     * @param beanName       생성할 Bean 이름
     * @param beanDefinition Bean 정의 정보
     * @return 생성된 Bean 객체
     */
    private Object autowireConstructor(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Constructor<?> autowiredConstructor = findAutowiredConstructor(beanClass);
        try {
            // @Autowired가 붙은 생성자가 있으면 의존성을 주입하여 인스턴스 생성
            if (autowiredConstructor != null) {
                Object[] parameters = resolveDependencies(autowiredConstructor);
                return autowiredConstructor.newInstance(parameters);
            } else {
                // 기본 생성자로 인스턴스 생성
                return beanClass.getDeclaredConstructor().newInstance();
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
     * DI 대상의 생성자 호출에 사용할 Argument 배열을 반환한다.
     *
     * <ul>
     *     <li>각 매개변수 타입에 맞춰 Bean 또는 프로퍼티 값을 조회한다.</li>
     *     <li>조회한 값을 Argument 배열에 담아 반환한다.</li>
     * </ul>
     * @param constructor DI 대상의 생성자
     * @return 생성자 호출에 사용할 Argument 배열
     */
    private Object[] resolveDependencies(Constructor<?> constructor) {
        Parameter[] params = constructor.getParameters();
        Object[] args = new Object[constructor.getParameterCount()];

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            if (param.isAnnotationPresent(Value.class)) {
                Value anno = param.getAnnotation(Value.class);
                args[i] = resolveEmbeddedValue(anno.value(), param.getType());
            } else {
                args[i] = resolveDependency(param.getType());
            }
        }

        return args;
    }

    /**
     * 주어진 프로퍼티 표현식으로부터 값을 조회해 값을 반환한다.
     *
     * @param value 프로퍼티 표현식 (예: "${server.port}")
     * @return 조회된 값
     */
    public String resolveEmbeddedValue(String value) {
        return resolveEmbeddedValue(value, String.class);
    }

    /**
     * 주어진 프로퍼티 표현식으로부터 값을 조회해 값을 반환한다.
     *
     * @param <T>          반환 대상의 타입
     * @param value        프로퍼티 표현식 (예: "${server.port}")
     * @param requiredType 의존성으로 주입할 대상 타입
     * @return 조회된 값
     */
    public <T> T resolveEmbeddedValue(String value, Class<T> requiredType) {
        return environment.getProperty(value, requiredType);
    }

    /**
     * 주어진 Class Type의 Bean을 찾아 의존성으로 주입할 인스턴스를 반환
     *
     * <p>지정된 Class Type을 구현, 상속한 Bean 목록을 조회한 후,
     * 적합한 인스턴스를 찾아 반환.
     *
     * @param requiredType 의존성으로 주입할 대상 타입
     * @param <T>          반환될 Bean의 타입 (제네릭)
     * @return 주어진 타입을 포함하는 Bean 인스턴스
     * @throws IllegalStateException Bean이 없거나 두 개 이상인 경우
     */
    private <T> T resolveDependency(Class<T> requiredType) {
        List<String> candidateBeanNames = getBeanNamesForType(requiredType);

        if (candidateBeanNames.isEmpty()) {
            throw new IllegalStateException(
                "No bean found of type [" + requiredType.getName() + "]. " +
                    "Unable to resolve a single candidate for dependency injection. ");
        }

        if (candidateBeanNames.size() > 1) {
            throw new IllegalStateException(
                "Multiple beans found for type [" + requiredType.getName() + "]. " +
                    "Unable to resolve a single candidate for dependency injection. " +
                    "Candidates: " + candidateBeanNames);
        }

        return getBean(candidateBeanNames.get(0), requiredType);
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