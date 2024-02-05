package com.example.dynamicgateway.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointUtilTest {

    @Test
    void testWthRemovedPrefix_remainsSame_ifNoIgnoredPrefixesPassed() {
        String path = "/auth/test-path";
        String returnedPath = EndpointUtil.withRemovedPrefix(path, Collections.emptyList());
        assertThat(returnedPath).isEqualTo(path);
    }

    @Test
    void testWthRemovedPrefix_returnsPathWithoutPrefix_ifStartsWithIgnoredPrefix() {
        String prefix = "/auth";
        String path = prefix + "/test-path";
        String returnedPath = EndpointUtil.withRemovedPrefix(path, List.of(prefix));
        String expectedPath = path.substring(prefix.length());
        assertThat(returnedPath).isEqualTo(expectedPath);
    }

    @Test
    void testWthRemovedPrefix_ifMultiplePrefixesMatch_returnsPathWithoutLongestIgnoredPrefix() {
        String prefix = "/auth";
        String longerPrefix = "/authorized";
        String path = longerPrefix + "/test-path";
        String returnedPath = EndpointUtil.withRemovedPrefix(path, List.of(prefix, longerPrefix));
        String expectedPath = path.substring(longerPrefix.length());
        assertThat(returnedPath).isEqualTo(expectedPath);
        String anotherReturnedPath = EndpointUtil.withRemovedPrefix(path, List.of(longerPrefix, prefix));
        assertThat(anotherReturnedPath).isEqualTo(expectedPath);
    }

    @Test
    void testWthRemovedPrefix_ifIgnoredPrefixSubstringOfPathFragment_returnsUnchangedPath() {
        String prefix = "/auth";
        String path = "/authorized/test-path";
        String returnedPath = EndpointUtil.withRemovedPrefix(path, List.of(prefix));
        assertThat(returnedPath).isEqualTo(path);
    }

    @Test
    void testExtractPrefix_returnsEmptyString_ifNoPrefixesPassed() {
        String prefix = "/auth";
        String path = prefix + "/test-path";
        String returnedPrefix = EndpointUtil.extractPrefix(path, Collections.emptyList());
        assertThat(returnedPrefix).isEqualTo("");
    }

    @Test
    void testExtractPrefix_returnsPrefix_ifPrefixContainedInPassedCollection() {
        String prefix = "/auth";
        String path = prefix + "/test-path";
        String returnedPrefix = EndpointUtil.extractPrefix(path, List.of(prefix));
        assertThat(returnedPrefix).isEqualTo(prefix);
    }

    @Test
    void testExtractPrefix_returnsLongestPrefix_ifMultiplePrefixesMatch() {
        String prefix = "/auth";
        String longerPrefix = "/authorized";
        String path = longerPrefix + "/test-path";
        String returnedPrefix = EndpointUtil.extractPrefix(path, List.of(prefix, longerPrefix));
        assertThat(returnedPrefix).isEqualTo(longerPrefix);
        String anotherReturnedPrefix = EndpointUtil.extractPrefix(path, List.of(longerPrefix, prefix));
        assertThat(anotherReturnedPrefix).isEqualTo(longerPrefix);
    }

    @Test
    void testExtractPrefix_ifIgnoredPrefixSubstringOfPathFragment_returnsEmptyString() {
        String prefix = "/auth";
        String path = "/authorized/test-path";
        String returnedPath = EndpointUtil.extractPrefix(path, List.of(prefix));
        assertThat(returnedPath).isEqualTo("");
    }
}