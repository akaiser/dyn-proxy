package de.kaiserv.dynproxy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ProxyUrlBuilder {
    private static final String
            _HOST_PARAM = "_host",
            _PATH_PARAM = "_path";

    private static final Collection<String>
            PROXY_PATTERN_PARAMS = Arrays.asList(_HOST_PARAM, _PATH_PARAM);

    private final Map<String, String> queryParams;

    ProxyUrlBuilder(String queryString) {
        queryParams = Stream.of(queryString.split("&"))
                .map(pair -> pair.split("="))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        buildProtocolAndHost(sb);
        buildQueryParams(sb);
        return sb.toString();
    }

    private void buildProtocolAndHost(StringBuilder sb) {
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
