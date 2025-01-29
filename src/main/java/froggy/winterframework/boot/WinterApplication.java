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

public class WinterApplication {

    private Class<?> mainApplicationClass;

    public WinterApplication(Class<?> mainApplicationClass) {
        this.mainApplicationClass = mainApplicationClass;
    }

    private ApplicationContext run() throws Exception{
        // 애플리케이션의 빈 관리 및 컨텍스트를 초기화하기 위한 기본 객체
        ApplicationContext context = new ApplicationContext();

        // 애플리케이션 실행에 필요한 빈을 수동으로 등록
        initContext(context);

        // 환경설정, 리스너 등록
        prepareContext(context);

        // BeanDefinition 정보 등록 및 실제 Bean Instance 생성
        // TODO: 의존성 주입 (DI) 로직 구현 필요
        refreshContext(context);

        // Embedded Web Application Server
        initServer(context);

        return context;
    }

    private void initContext(ApplicationContext context) {
        BeanFactory beanFactory = context.getBeanFactory();

        /**
         * RequestMappingHandlerMapping 수동 등록
         * 현재 DI 기능이 없어 수동으로 등록
         */

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

    private void refreshContext(ApplicationContext context) {
        registerBeanDefinition(context.getBeanFactory());
        finishBeanFactoryInitialization(context.getBeanFactory());
    }
    
    public void registerBeanDefinition(BeanFactory beanFactory) {
        Set<String> basePackageClassNames = getFullyQualifiedClassNamesByBasePackage();

        Set<BeanDefinition> beanDefinitions = findComponents(basePackageClassNames);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanFactory.registerBeanDefinition(beanDefinition.getBeanClass());
        }
    }

    private Set<String> getFullyQualifiedClassNamesByBasePackage() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("");

        File directory = new File(resource.getFile());

        Set<String> fullQualifiedClassNames = new LinkedHashSet<>();
        if (directory.exists() && directory.isDirectory()) {
            scanDirectory(directory, "", fullQualifiedClassNames);
        }
        return fullQualifiedClassNames;
    }

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

    private Set<BeanDefinition> findComponents(Set<String> ClassNames) {
        Set<BeanDefinition> beanDefinitions = new LinkedHashSet<>();

        for (String className : ClassNames) {
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

    private void finishBeanFactoryInitialization(BeanFactory beanFactory) {
        beanFactory.preInstantiateSingletons();
    }

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

    public static ApplicationContext run(Class<?> runClass) throws Exception {
        return (new WinterApplication(runClass)).run();
    }

}