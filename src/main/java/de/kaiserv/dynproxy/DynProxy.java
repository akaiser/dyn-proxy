package de.kaiserv.dynproxy;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

@WebServlet(asyncSupported = true)
public class DynProxy extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(DynProxy.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        var ctx = req.startAsync();
        ctx.start(() -> {
            HttpURLConnection conn = null;
            try {
                var proxyUrl = new ProxyUrlBuilder(req.getQueryString()).get();
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
        Collections.list(req.getHeaderNames())
                .forEach(name -> conn.setRequestProperty(name, req.getHeader(name)));
    }

    private void copyResponseHeaders(HttpURLConnection conn, HttpServletResponse resp) {
        conn.getHeaderFields().forEach((field, values) -> {
            var value = values.getFirst();
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

        try (var is = conn.getInputStream()) {
            copyInputStream(is, resp);
        } catch (Exception ex) {
            LOG.warn("Some minor explosion here... {}", ex.toString());

            try (var is = conn.getErrorStream()) {
                copyInputStream(is, resp);

                if (is == null) {
                    var stackTrace = Arrays.stream(ex.getStackTrace())
                            .map(StackTraceElement::toString)
                            .reduce((s1, s2) -> s1.concat("\n").concat(s2))
                            .orElse(ex.getMessage());

                    var error = ex.toString().concat("\n").concat(stackTrace);

                    try (var errorIs = new ByteArrayInputStream(error.getBytes())) {
                        resp.setContentType("text/plain");
                        copyInputStream(errorIs, resp);
                    }
                }
            }
        }
    }

    private void copyInputStream(InputStream is, HttpServletResponse resp) throws Exception {
        if (is != null) {
            var out = resp.getOutputStream();
            int n;
            byte[] buffer = new byte[4096];
            while (-1 != (n = is.read(buffer))) out.write(buffer, 0, n);
        }
    }
}
