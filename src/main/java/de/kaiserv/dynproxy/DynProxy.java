package de.kaiserv.dynproxy;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

@WebServlet(asyncSupported = true)
public class DynProxy extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(DynProxy.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        AsyncContext ctx = req.startAsync();
        ctx.start(() -> {
            HttpURLConnection conn = null;
            try {
                String proxyUrl = new ProxyUrlBuilder(req.getQueryString()).build();
                LOG.info("proxy to: {}", proxyUrl);

                conn = (HttpURLConnection) new URI(proxyUrl).toURL().openConnection();
                conn.setRequestMethod(req.getMethod());

                copyRequestHeaders(conn, req);
                copyResponseHeaders(conn, resp);
                copyResponse(conn, resp);
            } catch (Exception ex) {
                LOG.error("Some big explosion here...", ex);
            } finally {
                ctx.complete();
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private void copyRequestHeaders(HttpURLConnection conn, HttpServletRequest req) {
        Collections.list(req.getHeaderNames()).forEach(name ->
                conn.setRequestProperty(name, req.getHeader(name))
        );
    }

    private void copyResponseHeaders(HttpURLConnection conn, HttpServletResponse resp) {
        conn.getHeaderFields().forEach((field, values) -> {
            String value = values.getFirst();
            if (!"chunked".equals(value)) {
                resp.setHeader(field, value);
            }
        });
    }

    private void copyResponse(HttpURLConnection conn, HttpServletResponse resp) throws Exception {
        try {
            resp.setStatus(conn.getResponseCode());
        } catch (Exception ex) {
            resp.setStatus(500);
        }

        try (InputStream is = conn.getInputStream()) {
            copyInputStream(is, resp);
        } catch (Exception ex) {
            LOG.warn("Some minor explosion here... {}", ex.toString());

            try (InputStream is = conn.getErrorStream()) {
                copyInputStream(is, resp);

                if (is == null) {
                    String stackTrace = Arrays.stream(ex.getStackTrace())
                            .map(StackTraceElement::toString)
                            .reduce((s1, s2) -> s1.concat("\n").concat(s2))
                            .orElse(ex.getMessage());

                    String error = ex.toString().concat("\n").concat(stackTrace);

                    try (InputStream errorIs = new ByteArrayInputStream(error.getBytes())) {
                        resp.setContentType("text/plain");
                        copyInputStream(errorIs, resp);
                    }
                }
            }
        }
    }

    private void copyInputStream(InputStream is, HttpServletResponse resp) throws Exception {
        if (is != null) {
            OutputStream out = resp.getOutputStream();
            int n;
            byte[] buffer = new byte[4096];
            while (-1 != (n = is.read(buffer))) out.write(buffer, 0, n);
        }
    }
}
