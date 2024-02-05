package com.example.dynamicgateway.util;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class EndpointUtil {

    private static final Pattern pattern = Pattern.compile("/[^?#/]+");

    public static String withRemovedPrefix(String path, Collection<String> prefixes) {
        return findFragment(path, prefixes, PathFragmentPicker::pathWithoutPrefix);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String findFragment(String path, Collection<String> prefixes,
                                       PathFragmentPicker fragmentPicker) {
        validateArgs(path, prefixes);
        Matcher matcher = pattern.matcher(path);
        matcher.find();
        String firstPathFragment = matcher.group();
        for (String prefix : prefixes) {
            if (firstPathFragment.equals(prefix))
                return fragmentPicker.pick(prefix, path.substring(prefix.length()));
        }
        return fragmentPicker.pick("", path);

    }

    private static void validateArgs(String path, Collection<String> prefixes) {
        Stream.concat(Stream.of(path), prefixes.stream()).forEach(UriValidator::isValidPath);
    }

    public static String extractPrefix(String path, Collection<String> prefixes) {
        return findFragment(path, prefixes, PathFragmentPicker::prefix);
    }

    @FunctionalInterface
    private interface PathFragmentPicker {
        String pick(String prefix, String pathWithoutPrefix);

        static String prefix(String prefix, String pathWithoutPrefix) {
            return prefix;
        }

        static String pathWithoutPrefix(String prefix, String pathWithoutPrefix) {
            return pathWithoutPrefix;
        }
    }
}
