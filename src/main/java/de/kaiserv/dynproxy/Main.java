package de.kaiserv.dynproxy;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.apache.coyote.http2.Http2Protocol;

public class Main {

    public static void main(String... args) throws Exception {
        int port = Integer.parseInt(System.getProperty("proxy_port", "80"));

        var connector = new Connector(new Http11Nio2Protocol());
        connector.addUpgradeProtocol(new Http2Protocol());
        connector.setPort(port);

        var tomcat = new Tomcat();
        tomcat.setConnector(connector);

        var context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        Tomcat.addServlet(context, "DynProxy", new DynProxy());
        context.addServletMappingDecoded("/proxy", "DynProxy");

        tomcat.start();
        tomcat.getServer().await();
    }
}
