package com.example.dynamicgateway.util;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class EndpointUtil {

    private static final Pattern pattern = Pattern.compile("/[^?#/]+");

    public static String withRemovedPrefix(DocumentedEndpoint<?> endpoint, GatewayMeta meta) {
        return findFragment(endpoint, meta, PathFragmentPicker.pathWithoutPrefix());
    }

    private static String findFragment(DocumentedEndpoint<?> endpoint, GatewayMeta meta,
                                       PathFragmentPicker fragmentPicker) {
        String path = endpoint.getDetails().getPath();
        List<String> prefixes = meta.getIgnoredPrefixes();

        validateArgs(path, prefixes);

        return extractFragment(path, prefixes, fragmentPicker);

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String extractFragment(String path, List<String> prefixes,
                                          PathFragmentPicker fragmentPicker) {
        Matcher matcher = pattern.matcher(path);
        matcher.find();
        String firstPathFragment = matcher.group();
        for (String prefix : prefixes) {
            if (firstPathFragment.equals(prefix)) {
                String pathWithoutPrefix = path.substring(prefix.length());
                return fragmentPicker.pick(prefix, pathWithoutPrefix);
            }
        }
        return fragmentPicker.pick("", path);
    }

    private static void validateArgs(String path, List<String> prefixes) {
        Stream.concat(Stream.of(path), prefixes.stream()).forEach(UriValidator::isValidPath);
    }

    public static String extractPrefix(DocumentedEndpoint<?> endpoint, GatewayMeta meta) {
        return findFragment(endpoint, meta, PathFragmentPicker.prefix());
    }

    @FunctionalInterface
    private interface PathFragmentPicker {
        String pick(String prefix, String pathWithoutPrefix);

        static PathFragmentPicker prefix() {
            return (prefix, pathWithoutPrefix) -> prefix;
        }

        static PathFragmentPicker pathWithoutPrefix() {
            return (prefix, pathWithoutPrefix) -> pathWithoutPrefix;
        }
    }
}
