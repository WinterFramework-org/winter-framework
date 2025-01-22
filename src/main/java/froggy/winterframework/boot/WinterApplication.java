package froggy.winterframework.boot;

import froggy.winterframework.web.DispatcherServlet;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class WinterApplication {

    public static void run(Class<?> runClass) throws Exception {
        Server server = new Server(8080);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp"); // JSP 파일경로

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
        context.addServlet(JspServlet.class, "*.jsp");
        context.setWelcomeFiles(new String[]{"index.jsp"});

        context.addServlet(new ServletHolder(DispatcherServlet.class), "/");

        server.setHandler(context);

        // 서버 시작
        server.start();
        System.out.println("WinterFramework Server is running on http://localhost:8080");
        server.join();
    }

}
