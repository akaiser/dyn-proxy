package de.kaiserv.dynproxy;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.apache.coyote.http2.Http2Protocol;

import static java.util.Optional.ofNullable;

public class Main {

    public static void main(String... args) throws Exception {
        String port = ofNullable(System.getenv("PORT")).orElse("80");

        Connector connector = new Connector(new Http11Nio2Protocol());
        connector.addUpgradeProtocol(new Http2Protocol());
        connector.setPort(Integer.parseInt(port));

        Tomcat tomcat = new Tomcat();
        tomcat.setConnector(connector);

        Context context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        Tomcat.addServlet(context, "DynProxy", new DynProxy());
        context.addServletMappingDecoded("/proxy", "DynProxy");

        tomcat.start();
        tomcat.getServer().await();
    }
}
