package com.example.dynamicgateway.util;

import java.util.Collection;

public class EndpointUtil {
    public static String withRemovedPrefix(String path, Collection<String> prefixes) {
        for (String prefix : prefixes) {
            if (path.startsWith(prefix))
                return path.substring(prefix.length());
        }
        return path;
    }

    public static String extractPrefix(String path, Collection<String> prefixes) {
        for (String prefix : prefixes) {
            if (path.startsWith(prefix))
                return prefix;
        }
        return "";
    }
}
