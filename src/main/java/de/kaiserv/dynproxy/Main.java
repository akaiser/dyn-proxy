package de.kaiserv.dynproxy;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.apache.coyote.http2.Http2Protocol;

public class Main {

    public static void main(String... args) throws Exception {
        Connector connector = new Connector(new Http11Nio2Protocol());
        connector.addUpgradeProtocol(new Http2Protocol());
        connector.setPort(Integer.parseInt(System.getenv("PORT")));

        Tomcat tomcat = new Tomcat();
        tomcat.setConnector(connector);

        Context ctx = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        Tomcat.addServlet(ctx, DynProxy.class.getSimpleName(), new DynProxy());
        ctx.addServletMappingDecoded("/proxy", DynProxy.class.getSimpleName());

        tomcat.start();
        tomcat.getServer().await();
    }
}
