package de.kaiserv.dynproxy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProxyUrlBuilderTest {

    @Test
    public void builder_does_break_on_null_query_string() {
        var tested = new ProxyUrlBuilder(null);

        assertEquals("http://null", tested.get());
    }

    @Test
    public void builder_does_break_on_empty_query_string() {
        var tested = new ProxyUrlBuilder("");

        assertEquals("http://null", tested.get());
    }

    @Test
    public void builder_does_break_on_empty_host_param_value() {
        var tested = new ProxyUrlBuilder("_host=");

        assertEquals("http://null", tested.get());
    }

    @Test
    public void builder_does_not_require_port_and_path() {
        var tested = new ProxyUrlBuilder("_host=some.host");

        assertEquals("http://some.host", tested.get());
    }

    @Test
    public void builder_understands_https() {
        var tested = new ProxyUrlBuilder("_host=some.host:443");

        assertEquals("https://some.host:443", tested.get());
    }

    @Test
    public void builder_accepts_only_key_with_value_query_params() {
        var tested = new ProxyUrlBuilder("_host=some.host:80&_path=some/path&one=1&two");

        assertEquals("http://some.host:80/some/path?one=1", tested.get());
    }

    @Test
    public void builder_builds_expected_url() {
        var tested = new ProxyUrlBuilder("_host=some.host:5000&_path=some/path&one=1&two=2");

        assertEquals("http://some.host:5000/some/path?one=1&two=2", tested.get());
    }
}
