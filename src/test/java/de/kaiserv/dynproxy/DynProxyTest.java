package de.kaiserv.dynproxy;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynProxyTest {

    @Mock
    private HttpServletRequest request;

    private final DynProxy tested = new DynProxy();

    @Test
    public void buildProxyUrl_does_not_require_port_and_path() {
        when(request.getQueryString()).thenReturn("_host=some.host");

        assertEquals("http://some.host", tested.buildProxyUrl(request));
    }

    @Test
    public void buildProxyUrl_understands_https() {
        when(request.getQueryString()).thenReturn("_host=some.host:443");

        assertEquals("https://some.host:443", tested.buildProxyUrl(request));
    }

    @Test
    public void buildProxyUrl_accepts_only_key_with_value_query_params() {
        when(request.getQueryString()).thenReturn("_host=some.host:80&_path=some/path&some");

        assertEquals("http://some.host:80/some/path", tested.buildProxyUrl(request));
    }

    @Test
    public void buildProxyUrl_builds_expected_url() {
        when(request.getQueryString()).thenReturn("_host=some.host:5000&_path=some/path&one=1&two=2");

        assertEquals("http://some.host:5000/some/path?one=1&two=2", tested.buildProxyUrl(request));
    }
}
