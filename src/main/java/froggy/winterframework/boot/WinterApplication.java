package froggy.winterframework.boot;

import froggy.winterframework.beans.factory.config.BeanDefinition;
import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.stereotype.Component;
import froggy.winterframework.utils.WinterUtils;
import froggy.winterframework.web.DispatcherServlet;
import froggy.winterframework.web.servlet.handler.RequestMappingHandlerMapping;
import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

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
        // 애플리케이션의 빈 관리 및 컨텍스트를 초기화하기 위한 기본 객체
        ApplicationContext context = new ApplicationContext();

        // 필수 컴포넌트 수동 등록
        initContext(context);

        // 환경설정, 리스너 등록
        prepareContext(context);

        // @Component가 붙은 클래스 스캔, BeanDefinition 등록 및 Singleton Bean 인스턴스 생성
        refreshContext(context);

        // Embedded Web Application Server
        initServer(context);

        return context;
    }

    /**
     * 필수 Bean을 ApplicationContext에 등록.
     * <p>현재는 DI 기능이 없어 {@code RequestMappingHandlerMapping}을 수동 등록.
     *
     * @param context ApplicationContext
     */
    private void initContext(ApplicationContext context) {
        BeanFactory beanFactory = context.getBeanFactory();

        String beanName = WinterUtils.resolveSimpleBeanName(RequestMappingHandlerMapping.class);

        beanFactory.registerBeanDefinition(
            beanName,
            new BeanDefinition(RequestMappingHandlerMapping.class)
        );

        beanFactory.registerSingleton(
            beanName,
            new RequestMappingHandlerMapping(context)
        );
    }

    private void prepareContext(ApplicationContext context) {

    }

    /**
     * Bean 스캔 및 등록을 수행하고, Singleton Bean 인스턴스를 생성.
     *
     * @param context ApplicationContext
     */
    private void refreshContext(ApplicationContext context) {
        registerBeanDefinition(context.getBeanFactory());

        finishBeanFactoryInitialization(context.getBeanFactory());
    }

    /**
     * 기본 패키지를 스캔하여 @Component 애노테이션이 붙은 클래스의 {@link BeanDefinition} 등록.
     *
     * @param beanFactory BeanFactory 인스턴스
     */
    public void registerBeanDefinition(BeanFactory beanFactory) {
        Set<String> basePackageClassNames = getFullyQualifiedClassNamesByBasePackage();

        Set<BeanDefinition> beanDefinitions = findComponents(basePackageClassNames);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanFactory.registerBeanDefinition(beanDefinition.getBeanClass());
        }
    }


    /**
     * 패키지의 모든 클래스를 스캔하여 전체 클래스명을 Set으로 반환.
     *
     * @return 전체 클래스명을 담은 Set
     */
    private Set<String> getFullyQualifiedClassNamesByBasePackage() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("");

        File directory = new File(resource.getFile());

        Set<String> fullQualifiedClassNames = new LinkedHashSet<>();
        if (directory.exists() && directory.isDirectory()) {
            scanDirectory(directory, "", fullQualifiedClassNames);
        }
        return fullQualifiedClassNames;
    }

    /**
     * 주어진 디렉토리를 재귀적으로 스캔하여 .class 파일의 완전한 클래스명을 수집.
     *
     * @param directory               스캔할 디렉토리
     * @param packageName             현재 패키지 이름
     * @param fullQualifiedClassNames 클래스명을 저장할 Set
     */
    private void scanDirectory(File directory, String packageName, Set<String> fullQualifiedClassNames) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + file.getName() + ".", fullQualifiedClassNames);
            }

            if (file.getName().endsWith(".class")) {
                String className = packageName + file.getName().replace(".class", "");
                fullQualifiedClassNames.add(className);
            }
        }
    }

    /**
     * @Component 애노테이션이 붙은 클래스를 찾아 {@link BeanDefinition}로 변환하여 Set으로 반환.
     *
     * @param classNames 스캔된 클래스 이름 Set
     * @return BeanDefinition Set
     */
    private Set<BeanDefinition> findComponents(Set<String> classNames) {
        Set<BeanDefinition> beanDefinitions = new LinkedHashSet<>();

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (WinterUtils.hasAnnotation(clazz, Component.class)) {
                    beanDefinitions.add(new BeanDefinition(clazz));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found: " + className, e);
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
        Server server = new Server(8080);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase("src/main/webapp"); // JSP 파일경로

        /*
        * JSP Configuration
        * AnnotationConfiguration - 애노테이션 기반의 설정
        */
        Configuration.ClassList classList = Configuration.ClassList.setServerDefault(server);
        classList.addBefore(
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
            "org.eclipse.jetty.annotations.AnnotationConfiguration"
        );

        // jsp요청을 JspServlet에 매핑
        webAppContext.addServlet(JspServlet.class, "*.jsp");
        webAppContext.setWelcomeFiles(new String[]{"index.jsp"});

        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        webAppContext.addServlet(new ServletHolder(dispatcherServlet), "/");

        server.setHandler(webAppContext);

        server.start();
        System.out.println("WinterFramework Server is running on http://localhost:8080");
        server.join();
    }

    /**
     * 애플리케이션 실행 메소드
     */
    public static ApplicationContext run(Class<?> runClass) throws Exception {
        return (new WinterApplication(runClass)).run();
    }

}