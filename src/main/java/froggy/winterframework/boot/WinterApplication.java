package froggy.winterframework.boot;

import static froggy.winterframework.utils.WinterUtils.hasAnnotation;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.beans.factory.config.BeanFactoryPostProcessor;
import froggy.winterframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.boot.web.embedded.jetty.jettyWebServer;
import froggy.winterframework.boot.web.server.WebServer;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.annotation.ConfigurationClassPostProcessor;
import froggy.winterframework.core.PropertySource;
import froggy.winterframework.core.env.Environment;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.utils.WinterUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 프레임워크의 실행을 담당하는 메인 클래스.
 */
public class WinterApplication {

    /**
     * 애플리케이션 진입점 클래스.
     */
    private Class<?> mainApplicationClass;

    public WinterApplication(Class<?> mainApplicationClass) {
        this.mainApplicationClass = mainApplicationClass;
    }

    /**
     * 애플리케이션을 실행하여 {@link ApplicationContext}를 초기화하고 반환.
     *
     * @return 초기화된 {@link ApplicationContext}
     * @throws Exception 실행 중 발생하는 예외
     */
    private ApplicationContext run() throws Exception {
        // 애플리케이션 실행에 필요한 환경(Environment) 구성
        Environment environment = prepareEnvironment();

        // 애플리케이션의 빈 관리 및 컨텍스트를 초기화하기 위한 기본 객체
        ApplicationContext context = new ApplicationContext();

        // 환경설정, 리스너 등록
        prepareContext(context, environment);

        // @Component가 붙은 클래스 스캔, BeanDefinition 등록 및 Singleton Bean 인스턴스 생성
        refreshContext(context);

        // Embedded Web Application Server
        initServer(context);

        return context;
    }

    /**
     * 애플리케이션 실행에 필요한 환경(Environment) 구성
     */
    private Environment prepareEnvironment() throws IOException {
        Environment environment = new Environment();

        // basePackage 프로퍼티 설정
        HashMap<String, String> property = new HashMap<>();
        property.put("basePackage", mainApplicationClass.getPackage().getName());
        environment.getPropertySource().mergePropertySource(new PropertySource("system", property));

        return environment;
    }

    /**
     * 1) Environment를 바인드 및 빈으로 등록하고,
     * 2) 초기화 단계에서 사용할 BeanFactoryPostProcessor를 추가.
     */
    private void prepareContext(ApplicationContext context, Environment environment) {
        context.addEnvironment(environment);

        registerEnvironmentBean(context, environment);
        applyBeanFactoryPostProcessor(context);
    }

    private void registerEnvironmentBean(ApplicationContext context, Environment environment) {
        BeanFactory beanFactory = context.getBeanFactory();

        String beanName = WinterUtils.resolveSimpleBeanName(Environment.class);

        beanFactory.registerBeanDefinition(
            beanName,
            new BeanDefinition(Environment.class)
        );

        beanFactory.registerSingleton(
            beanName,
            environment
        );
    }

    /**
     * 컨텍스트 초기화 과정에서 내부적으로 사용될 BeanFactoryPostProcessor를 등록.
     *
     * @param context
     */
    private void applyBeanFactoryPostProcessor(ApplicationContext context) {
        context.addBeanFactoryPostProcessor(new ConfigurationClassPostProcessor());
    }

    /**
     * Bean 정의를 스캔·등록하고, BeanFactoryPostProcessor 및 BeanPostProcessor를 실행한 뒤,
     * 최종적으로 모든 싱글톤 Bean을 생성하여 초기화 진행
     *
     * @param context ApplicationContext
     */
    private void refreshContext(ApplicationContext context) {
        prepareBeanFactory(context.getBeanFactory(), context.getEnvironment(), context);

        registerBeanDefinition(context.getBeanFactory());

        postProcessBeanFactory(context.getBeanFactory(), context.getBeanFactoryPostProcessors());

        finishBeanFactoryInitialization(context.getBeanFactory());
    }

    private void prepareBeanFactory(BeanFactory beanFactory, Environment environment, ApplicationContext context) {
        beanFactory.addEnvironment(environment);
        beanFactory.registerResolvableDependency(ApplicationContext.class, context);
    }

    /**
     * 등록된 BeanFactoryPostProcessor들을 조회하여, 대상 BeanFactory에 대해 후처리 작업을 실행
     *
     * @param factory 후처리 작업이 수행될 BeanFactory 인스턴스
     */
    private void postProcessBeanFactory(BeanFactory factory, List<BeanFactoryPostProcessor> postProcessors) {
        List<BeanFactoryPostProcessor> allProcessors = new ArrayList<>(postProcessors);
        for (String ppName : factory.getBeanNamesForType(BeanFactoryPostProcessor.class)) {
            allProcessors.add(factory.getBean(ppName, BeanFactoryPostProcessor.class));
        }

        // 1. BeanFactoryPostProcessor#postProcessBeanDefinitionRegistry() 실행
        for (BeanFactoryPostProcessor pp : allProcessors) {
            if (pp instanceof BeanDefinitionRegistryPostProcessor) {
                BeanDefinitionRegistryPostProcessor bdpp = (BeanDefinitionRegistryPostProcessor) pp;
                bdpp.postProcessBeanDefinitionRegistry(factory);
            }
        }

        // 2. BeanFactoryPostProcessor#postProcessBeanFactory 실행
        for (BeanFactoryPostProcessor pp : allProcessors) {
            pp.postProcessBeanFactory(factory);
        }
    }

    /**
     * 기본 패키지를 스캔하여 @Component 애노테이션이 붙은 클래스의 {@link BeanDefinition} 등록.
     *
     * @param beanFactory BeanFactory 인스턴스
     */
    public void registerBeanDefinition(BeanFactory beanFactory) {
        Set<Class<?>> classNames = scanBeanCandidates();

        Set<BeanDefinition> beanDefinitions = createBeanDefinitions(classNames);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanFactory.registerBeanDefinition(beanDefinition.getBeanClass());
        }
    }

    /**
     * 애플리케이션과 외부 라이브러리에서
     * Bean 등록 대상 클래스를 스캔하여 반환한다.
     *
     * @return 스캔된 Bean 후보 클래스들의 Set
     */
    private Set<Class<?>> scanBeanCandidates() {
        Set<Class<?>> appBeans = WinterUtils.scanTypesAnnotatedWith(
            Component.class,
            mainApplicationClass.getPackage().getName()
        );

        Set<Class<?>> classes = WinterUtils.scanTypesAnnotatedWith(
            Component.class,
            "froggy.winterframework"
        );

        appBeans.addAll(classes);

        Set<Class<?>> externalBeans = scanAutoConfigClasses();
        appBeans.addAll(externalBeans);

        return appBeans;
    }


    /**
     * 외부 패키지의 모든 클래스를 스캔하여 FQCN의 Set으로 반환.
     *
     * @return FQCN의 Set
     */
    private Set<Class<?>> scanAutoConfigClasses() {
        Set<Class<?>> autoConfigClasses = new HashSet<>();
        try {
            Enumeration<URL> configFiles = ClassLoader
                .getSystemResources("META-INF/winter.autoconfig");
            while (configFiles.hasMoreElements()) {
                URL resource = configFiles.nextElement();
                for (String className : readClassName(resource)) {
                    autoConfigClasses.add(loadClass(className));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                "META-INF/winter.autoconfig 읽기 실패: " + e.getMessage(), e);
        }
        return autoConfigClasses;
    }

    private Set<String> readClassName(URL url) throws IOException {
        Set<String> classNames = new HashSet<>();

        try (InputStream is = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String className;
            while ((className = reader.readLine()) != null) {
                className = className.trim();
                if (className.isEmpty() || className.startsWith("//")) continue;

                classNames.add(className);
            }
        }

        return classNames;
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false,
                Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "auto-config 클래스 로드 실패: " + className, e);
        }
    }

    /**
     * Bean 후보 클래스 목록을 순회하면서
     * 각 클래스의 BeanDefinition을 생성하여 반환한다.
     *
     * @param candidateClasses Bean 후보 클래스들의 Set
     * @return BeanDefinition Set
     */
    private Set<BeanDefinition> createBeanDefinitions(Set<Class<?>> candidateClasses) {
        Set<BeanDefinition> beanDefinitions = new LinkedHashSet<>();

        for (Class<?> clazz : candidateClasses) {
            if (hasAnnotation(clazz, Component.class)) {
                beanDefinitions.add(new BeanDefinition(clazz));
            }
        }

        return beanDefinitions;
    }

    /**
     * 등록된 {@link BeanDefinition} 기반으로 빈을 인스턴스를 생성.
     *
     * @param beanFactory {@link BeanFactory} 인스턴스
     */
    private void finishBeanFactoryInitialization(BeanFactory beanFactory) {
        beanFactory.preInstantiateSingletons();
    }

    /**
     * Embedded WAS 실행
     */
    private void initServer(ApplicationContext applicationContext) throws Exception {
        WebServer webServer = new jettyWebServer(applicationContext);
        webServer.start();
    }

    /**
     * 애플리케이션 실행 메소드
     */
    public static ApplicationContext run(Class<?> runClass) throws Exception {
        return (new WinterApplication(runClass)).run();
    }

}