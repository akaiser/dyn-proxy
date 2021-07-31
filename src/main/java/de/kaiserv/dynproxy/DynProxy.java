package de.kaiserv.dynproxy;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(asyncSupported = true)
public class DynProxy extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(DynProxy.class);

    private static final String
            _HOST_PARAM = "_host",
            _PATH_PARAM = "_path";

    private static final Collection<String>
            PROXY_PATTERN_PARAMS = Arrays.asList(_HOST_PARAM, _PATH_PARAM);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        AsyncContext ctx = req.startAsync();
        ctx.start(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(buildProxyUrl(req)).openConnection();
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

     String buildProxyUrl(HttpServletRequest req) {
        Map<String, String> queryParams = Stream.of(req.getQueryString().split("&"))
                .map(pair -> pair.split("="))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));

        String proxyUrl = buildProtocolAndHost(queryParams) + buildQueryParams(queryParams);

        LOG.info("proxy to: {}", proxyUrl);
        return proxyUrl;
    }

    private String buildProtocolAndHost(Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder();

        sb.append("http");
        String host = queryParams.get(_HOST_PARAM);
        String[] hostParts = host.split(":");
        if (hostParts.length == 2) {
            if ("443".equals(hostParts[1])) {
                sb.append("s");
            }
        }
        sb.append("://").append(host);
        Optional.ofNullable(queryParams.get(_PATH_PARAM)).ifPresent(path -> sb.append("/").append(path));

        return sb.toString();
    }

    private String buildQueryParams(Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder();

        queryParams.forEach((param, value) -> {
            if (!PROXY_PATTERN_PARAMS.contains(param)) {
                sb.append(sb.lastIndexOf("?") == -1 ? '?' : '&');
                sb.append(String.format("%s=%s", param, value));
            }
        });

        return sb.toString();
    }

    private void copyRequestHeaders(HttpURLConnection conn, HttpServletRequest req) {
        Collections.list(req.getHeaderNames()).forEach(name ->
                conn.setRequestProperty(name, req.getHeader(name))
        );
    }

    private void copyResponseHeaders(HttpURLConnection conn, HttpServletResponse resp) {
        conn.getHeaderFields().forEach((field, values) -> {
            String value = values.get(0);
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

                    try (InputStream errorIS = new ByteArrayInputStream(error.getBytes())) {
                        resp.setContentType("text/plain");
                        copyInputStream(errorIS, resp);
                    }
                }
            }
        }
    }

    private void copyInputStream(InputStream is, HttpServletResponse resp) throws Exception {
        if (is != null) {
            ServletOutputStream out = resp.getOutputStream();
            int n;
            byte[] buffer = new byte[4096];
            while (-1 != (n = is.read(buffer))) out.write(buffer, 0, n);
        }
    }
}
