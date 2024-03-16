package com.example.dynamicgateway.util;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A utility class for convenient extraction of path components of {@link DocumentedEndpoint}s
 */
public class EndpointUtil {
    private static final Pattern PATH_SEGMENT_PATTERN = Pattern.compile("/[^?#/]+");

    private EndpointUtil() {
    }

    /**
     * A shorthand for {@code EndpointUtil.pathWithRemovedPrefix(endpoint.getDetails().getPath(), gatewayMeta)}
     *
     * @see EndpointUtil#pathWithRemovedPrefix(String, GatewayMeta)
     */
    @NonNull
    public static String pathWithRemovedPrefix(@NonNull DocumentedEndpoint<?> endpoint,
                                               @NonNull GatewayMeta meta) {
        String path = endpoint.getDetails().getPath();
        return pathWithRemovedPrefix(path, meta);
    }

    /**
     * Returns a string corresponding to the path without the longest matching
     * ignored prefix of those returned by {@link GatewayMeta#getIgnoredPrefixes()}.
     * If the path doesn't start with any of the ignored prefixes, returns the
     * original path string
     *
     * @param endpointPath path that should be trimmed
     * @param meta     supplier of ignored prefixes
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @NonNull
    public static String pathWithRemovedPrefix(@NonNull String endpointPath,
                                               @NonNull GatewayMeta meta) {
        return findSegment(endpointPath, meta, PathSegmentPicker.pathWithoutPrefix());
    }

    private static String findSegment(String path, GatewayMeta meta,
                                      PathSegmentPicker segmentPicker) {
        Stream.of(path, meta, segmentPicker).forEach(Objects::requireNonNull);

        List<String> prefixes = meta.getIgnoredPrefixes();

        validateSegments(path, prefixes);

        return extractSegment(path, prefixes, segmentPicker);
    }

    private static void validateSegments(String path, List<String> prefixes) {
        Stream.concat(Stream.of(path), prefixes.stream()).forEach(UriValidator::isValidPath);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String extractSegment(String path, List<String> prefixes,
                                         PathSegmentPicker segmentPicker) {
        Matcher matcher = PATH_SEGMENT_PATTERN.matcher(path);
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

    /**
     * A shorthand for {@code EndpointUtil.pathPrefix(endpoint.getDetails().getPath(), gatewayMeta)}
     *
     * @see EndpointUtil#pathWithRemovedPrefix(String, GatewayMeta)
     */
    @NonNull
    public static String pathPrefix(@NonNull DocumentedEndpoint<?> endpoint,
                                    @NonNull GatewayMeta meta) {
        String path = endpoint.getDetails().getPath();
        return pathPrefix(path, meta);
    }

    /**
     * Returns a string corresponding to the path's longest matching
     * ignored prefix of those returned by {@link GatewayMeta#getIgnoredPrefixes()}.
     * If the path doesn't start with any of the ignored prefixes, returns
     * an empty string
     *
     * @param endpointPath endpoint path that should be examined
     * @param meta     supplier of ignored prefixes
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @NonNull
    public static String pathPrefix(@NonNull String endpointPath,
                                    @NonNull GatewayMeta meta) {
        return findSegment(endpointPath, meta, PathSegmentPicker.prefix());
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
