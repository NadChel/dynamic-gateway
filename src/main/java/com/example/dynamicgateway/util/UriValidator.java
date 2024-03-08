package com.example.dynamicgateway.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating URI schemes and paths. Though overall it respects the
 * RFC 3986 specification, this class takes the liberty of diverging from it in the following
 * ways:
 * <p>
 * 1. A scheme's definition is extended to include the characters separating it from the authority,
 * specifically a colon and two forward slashes.
 * For example, in {@code http://example.com} the scheme would be {@code http://}, not {@code http}.
 * The latter would <em>not</em> be considered a valid scheme by this class
 * <p>
 * 2. Trailing slashes are <em>not</em> allowed for paths. A special case for this rule is that
 * {@code /} is <i>not</i> considered a valid path
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC 3986</a>
 */
public class UriValidator {
    private UriValidator() {
    }

    /**
     * Checks the validity of a URI path throwing an exception if the check fails
     *
     * @param path URI path for validation
     * @throws IllegalArgumentException if the passed path is invalid
     */
    public static void requireValidPath(String path) {
        boolean matchesPathPattern = isValidPath(path);
        if (!matchesPathPattern) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
    }

    /**
     * Checks if a URI path is valid
     *
     * @param path URI path
     * @return {@code true} if the path is valid, {@code false} otherwise
     */
    public static boolean isValidPath(String path) {
        return Pattern.matches("/[^?#]+[^/?#]|", path);
    }

    /**
     * Checks the validity of a URI scheme throwing an exception if the check fails
     *
     * @param scheme URI scheme for validation
     * @throws IllegalArgumentException if the passed scheme is invalid
     */
    public static void requireValidScheme(String scheme) {
        boolean matchesSchemePattern = isValidScheme(scheme);
        if (!matchesSchemePattern) {
            throw new IllegalArgumentException("Invalid scheme: " + scheme);
        }
    }

    /**
     * Checks if a URI scheme is valid
     *
     * @param scheme URI scheme
     * @return {@code true} if the scheme is valid, {@code false} otherwise
     */
    public static boolean isValidScheme(String scheme) {
        return Pattern.matches("[A-Za-z][A-Za-z+.-]+://", scheme);
    }
}
