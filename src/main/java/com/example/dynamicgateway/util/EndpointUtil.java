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
        return findSegment(endpoint, meta, PathSegmentPicker.pathWithoutPrefix());
    }

    private static String findSegment(DocumentedEndpoint<?> endpoint, GatewayMeta meta,
                                      PathSegmentPicker segmentPicker) {
        String path = endpoint.getDetails().getPath();
        List<String> prefixes = meta.getIgnoredPrefixes();

        validateSegments(path, prefixes);

        return extractSegment(path, prefixes, segmentPicker);

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String extractSegment(String path, List<String> prefixes,
                                         PathSegmentPicker segmentPicker) {
        Matcher matcher = pattern.matcher(path);
        matcher.find();
        String firstPathSegment = matcher.group();
        for (String prefix : prefixes) {
            if (firstPathSegment.equals(prefix)) {
                String pathWithoutPrefix = path.substring(prefix.length());
                return segmentPicker.pick(prefix, pathWithoutPrefix);
            }
        }
        return segmentPicker.pick("", path);
    }

    private static void validateSegments(String path, List<String> prefixes) {
        Stream.concat(Stream.of(path), prefixes.stream()).forEach(UriValidator::isValidPath);
    }

    public static String extractPrefix(DocumentedEndpoint<?> endpoint, GatewayMeta meta) {
        return findSegment(endpoint, meta, PathSegmentPicker.prefix());
    }

    @FunctionalInterface
    private interface PathSegmentPicker {
        String pick(String prefix, String pathWithoutPrefix);

        static PathSegmentPicker prefix() {
            return (prefix, pathWithoutPrefix) -> prefix;
        }

        static PathSegmentPicker pathWithoutPrefix() {
            return (prefix, pathWithoutPrefix) -> pathWithoutPrefix;
        }
    }
}
