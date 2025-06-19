package froggy.winterframework.boot.web.embedded.jetty;

import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.boot.web.server.WebServer;
import froggy.winterframework.boot.web.servlet.FilterRegistrationBean;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.core.env.Environment;
import froggy.winterframework.web.DispatcherServlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class jettyWebServer implements WebServer {

    private final ApplicationContext context;
    private Server server;

    private int serverPort;
    private boolean sslEnabled;
    private String keyStorePath;
    private String keyStoreType;
    private String keyStorePassword;

    public jettyWebServer(ApplicationContext context) {
        this.context = context;
        init(context);
    }
    public void init(ApplicationContext context) {
        Environment environment = context.getEnvironment();
        this.serverPort         = environment.getProperty("server.port", Integer.class, 8080);
        this.sslEnabled         = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        if (sslEnabled) {
            this.keyStorePath       = environment.getProperty("server.ssl.key-store", String.class);
            this.keyStoreType       = environment.getProperty("server.ssl.key-store-type", String.class);
            this.keyStorePassword   = environment.getProperty("server.ssl.key-store-password", String.class);
        }
    }

    @Override
    public void start() throws Exception {
        server = new Server();

        ServerConnector connector = createConnector();
        server.addConnector(connector);

        WebAppContext webAppContext = createWebAppContext();

        // Cors 필터 등록
        addFilter(webAppContext);

        // jsp요청을 JspServlet에 매핑
        configureServlets(webAppContext);

        server.setHandler(webAppContext);

        server.start();
        System.out.println("WinterFramework Server is running");
        System.out.println("http://localhost:" + serverPort);
        server.join();
    }

    private void configureServlets(WebAppContext webAppContext) {
        webAppContext.addServlet(JspServlet.class, "*.jsp");
        webAppContext.setWelcomeFiles(new String[]{"index.jsp"});
        webAppContext.addServlet(new ServletHolder(new DispatcherServlet(context)), "/");
    }

    private void addFilter(WebAppContext webAppContext) {
        BeanFactory beanFactory = context.getBeanFactory();

        List<String> filterRegNames = beanFactory.getBeanNamesForType(FilterRegistrationBean.class);

        List<FilterRegistrationBean<Filter>> filterBeans = new ArrayList<>();
        for (String filterRegName : filterRegNames) {
            FilterRegistrationBean<Filter> bean = beanFactory.getBean(filterRegName, FilterRegistrationBean.class);
            filterBeans.add(bean);
        }

        Collections.sort(filterBeans, new Comparator<FilterRegistrationBean<Filter>>() {
            @Override
            public int compare(FilterRegistrationBean<Filter> a, FilterRegistrationBean<Filter> b) {
                return Integer.compare(a.getOrder(), b.getOrder());
            }
        });

        for (FilterRegistrationBean<Filter> bean : filterBeans) {
            Filter filter = bean.getFilter();
            FilterHolder filterHolder = new FilterHolder(filter);
            EnumSet<DispatcherType> dispatcherTypes = bean.getDispatcherTypes();

            for (String urlPattern : bean.getUrlPatterns()) {
                webAppContext.addFilter(filterHolder, urlPattern, dispatcherTypes);
            }
        }
    }

    private WebAppContext createWebAppContext() {
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
        return webAppContext;
    }

    private ServerConnector createConnector() {
        // form-urlencoded 요청을 POST, PUT, PATCH, DELETE에서도 파싱 가능하게 설정
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setFormEncodedMethods("POST", "PUT", "PATCH", "DELETE");

        if(sslEnabled) {
            return createSslConnector(server);
        } else {
            ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
            connector.setPort(serverPort);
            return connector;
        }

    }

    private ServerConnector createSslConnector(Server server) {
        // 1. SslContextFactory 설정
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyStoreType(keyStoreType);

        // 2. HTTPS HttpConfiguration
        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(serverPort);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // 3. 연결 팩토리 구성 (SSL → HTTP/1.1)
        SslConnectionFactory sslConnectionFactory =
            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.toString());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpsConfig);

        // 4. ServerConnector 생성
        ServerConnector sslConnector = new ServerConnector(
            server, sslConnectionFactory, httpConnectionFactory
        );
        sslConnector.setPort(serverPort);
        sslConnector.setIdleTimeout(30000);

        return sslConnector;
    }

    @Override
    public void stop() {
        try {
            // 1. 그레이스풀 셧다운: 최대 5초 동안 열린 요청을 처리하고 커넥션을 정리
            server.setStopAtShutdown(true);
            server.setStopTimeout(5_000);

            // 2. 서버 정지 요청
            server.stop();

            // 3. 실제 스레드 종료를 대기
            server.join();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to stop Web Server.", ex);
        }
    }
}
