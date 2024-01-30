package com.example.dynamicgateway.util;

import java.util.regex.Pattern;

public class UriValidator {
    public static void requireValidPath(String docPath) {
        boolean matchesPathPattern = isValidPath(docPath);
        if (!matchesPathPattern) {
            throw new IllegalArgumentException("Invalid path: " + docPath);
        }
    }

    public static boolean isValidPath(String path) {
        return Pattern.matches("(/[A-Za-z\\d-]+)+", path);
    }

    public static void requireValidScheme(String scheme) {
        boolean matchesSchemePattern = isValidScheme(scheme);
        if (!matchesSchemePattern) {
            throw new IllegalArgumentException("Invalid scheme: " + scheme);
        }
    }

    public static boolean isValidScheme(String scheme) {
        return Pattern.matches("[a-z]+://", scheme);
    }
}
