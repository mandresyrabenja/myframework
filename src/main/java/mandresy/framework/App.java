package mandresy.framework;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class App {
    public static void start() throws LifecycleException {
        String webappDirectory = "src/main/webapp";

        Tomcat tomcat = new Tomcat();

        StandardContext ctx = (StandardContext) tomcat.addWebapp(
                "/",
                new File(webappDirectory)
                        .getAbsolutePath()
        );
        System.out.println(
                "Configuration de l'application avec le dossier de base: "
                        + new File("./" + webappDirectory).getAbsolutePath()
        );

        // Declare an alternative location for your "WEB-INF/classes" dir
        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(
                new DirResourceSet(resources, "/WEB-INF/classes",
                        additionWebInfClasses.getAbsolutePath(), "/")
        );
        ctx.setResources(resources);

        tomcat.getConnector();
        tomcat.setPort(8080);
        tomcat.start();
        tomcat.getServer().await();
    }
}
