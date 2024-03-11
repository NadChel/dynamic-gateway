package com.example.dynamicgateway.model.authorizationHeader;

import com.example.dynamicgateway.exception.AuthenticationSchemeNotFoundException;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * An object representing an {@code Authorization} HTTP header
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7235#section-2">RFC 7235</a>
 */
@Getter
public class AuthorizationHeader {
    public static final String BEARER_SPACE = "bearer ";
    public static final String BASIC_SPACE = "basic ";
    private final String scheme;
    private final String credentials;

    private AuthorizationHeader(String scheme, String credentials) {
        this.scheme = scheme;
        this.credentials = credentials;
    }

    /**
     * Parses the header string and returns an equivalent {@code AuthorizationHeader} instance
     * <p>
     * The string must be in the format {@code "<scheme> <credentials>"}, for example {@code "bearer 12345"}.
     * Extra spaces are ignored. For instance, {@code "    bearer   12345   "} maps to the same (equal) {@code AuthorizationHeader}
     * object as {@code "bearer 12345"}
     *
     * @return an {@code AuthorizationHeader} that matches the passed-in string or, in case the argument is
     * {@code null}, an {@code AuthorizationHeader} with schemes and credentials equal to empty strings
     * @throws AuthenticationSchemeNotFoundException if the string doesn't specify an authentication scheme
     */
    @NonNull
    public static AuthorizationHeader fromString(@Nullable String authorizationHeaderString) {
        String scheme, credentials;
        if (authorizationHeaderString == null) {
            scheme = "";
            credentials = "";
        } else {
            String trimmedHeader = authorizationHeaderString.trim();
            int indexOfSpace = trimmedHeader.indexOf(" ");
            if (indexOfSpace < 0) {
                throw new AuthenticationSchemeNotFoundException();
            }
            scheme = trimmedHeader.substring(0, indexOfSpace);
            credentials = trimmedHeader.substring(indexOfSpace).trim();
        }
        return new AuthorizationHeader(scheme, credentials);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorizationHeader that)) return false;
        return Objects.equals(scheme, that.scheme) &&
                Objects.equals(credentials, that.credentials);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(scheme, credentials);
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} {1}", getScheme(), getCredentials());
    }

    @Override
    @SuppressWarnings("deprecation")
    // protection against finalizer attacks, see SEI CERT OBJ11-J
    protected final void finalize() {
    }
}
