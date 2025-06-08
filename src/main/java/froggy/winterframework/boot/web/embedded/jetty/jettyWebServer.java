package froggy.winterframework.boot.web.embedded.jetty;

import froggy.winterframework.boot.web.server.WebServer;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.core.env.Environment;
import froggy.winterframework.web.DispatcherServlet;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class jettyWebServer implements WebServer {

    private final ApplicationContext context;
    private Server server;

    private int serverPort;

    public jettyWebServer(ApplicationContext context) {
        this.context = context;
        init(context);
    }
    public void init(ApplicationContext context) {
        Environment environment = context.getEnvironment();
        this.serverPort         = environment.getProperty("server.port", Integer.class, 8080);
    }

    @Override
    public void start() throws Exception {
        server = new Server();

        ServerConnector connector = createConnector();
        server.addConnector(connector);

        WebAppContext webAppContext = createWebAppContext();

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

        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(serverPort);
        return connector;
    }


    @Override
    public void stop() {
        try {
            server.setStopAtShutdown(true);
            server.setStopTimeout(5_000);

            server.stop();

            server.join();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to stop Web Server.", ex);
        }
    }
}
