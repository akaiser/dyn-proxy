package de.kaiserv.dynproxy;

import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

final class ProxyUrlBuilder {
    private static final String _HOST_PARAM = "_host", _PATH_PARAM = "_path";

    private static final Collection<String> PROXY_PATTERN_PARAMS = Arrays.asList(_HOST_PARAM, _PATH_PARAM);

    private final Map<String, String> queryParams;

    ProxyUrlBuilder(@Nullable String queryString) {
        var query = ofNullable(queryString).orElse("");
        queryParams = Stream.of(query.split("&"))
                .map(pair -> pair.split("="))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
    }

    public String get() {
        var sb = new StringBuilder();
        buildProtocolAndHost(sb);
        buildQueryParams(sb);
        return sb.toString();
    }

    private void buildProtocolAndHost(StringBuilder sb) {
        sb.append("http");
        var host = queryParams.get(_HOST_PARAM);
        var hostParts = ofNullable(host)
                .map((it) -> it.split(":"))
                .orElse(new String[0]);

        if (hostParts.length == 2) {
            if ("443".equals(hostParts[1])) {
                sb.append("s");
            }
        }
        sb.append("://").append(host);
        ofNullable(queryParams.get(_PATH_PARAM)).ifPresent(path -> sb.append("/").append(path));
    }

    private void buildQueryParams(StringBuilder sb) {
        queryParams.forEach((param, value) -> {
            if (!PROXY_PATTERN_PARAMS.contains(param)) {
                sb.append(sb.lastIndexOf("?") == -1 ? '?' : '&');
                sb.append(String.format("%s=%s", param, value));
            }
        });
    }
}
