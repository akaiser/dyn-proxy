package de.kaiserv.dynproxy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProxyUrlBuilderTest {

    @Test
    public void builder_does_not_require_port_and_path() {
        ProxyUrlBuilder tested = new ProxyUrlBuilder("_host=some.host");

        assertEquals("http://some.host", tested.build());
    }

    @Test
    public void builder_understands_https() {
        ProxyUrlBuilder tested = new ProxyUrlBuilder("_host=some.host:443");

        assertEquals("https://some.host:443", tested.build());
    }

    @Test
    public void builder_accepts_only_key_with_value_query_params() {
        ProxyUrlBuilder tested = new ProxyUrlBuilder("_host=some.host:80&_path=some/path&one=1&two");

        assertEquals("http://some.host:80/some/path?one=1", tested.build());
    }

    @Test
    public void builder_builds_expected_url() {
        ProxyUrlBuilder tested = new ProxyUrlBuilder("_host=some.host:5000&_path=some/path&one=1&two=2");

        assertEquals("http://some.host:5000/some/path?one=1&two=2", tested.build());
    }
}
